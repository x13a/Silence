package me.lucky.silence.screening

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import android.telecom.Call
import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import me.lucky.silence.AppDatabase
import me.lucky.silence.Contact
import me.lucky.silence.Group
import me.lucky.silence.Message
import me.lucky.silence.Preferences

class CallScreeningHelper(private val ctx: Context) {
    private val telephonyManager = ctx.getSystemService(TelephonyManager::class.java)
    private val prefs = Preferences(ctx)
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    private val db = AppDatabase.getInstance(ctx).allowNumberDao()

    fun check(number: PhoneNumber, callDetails: Call.Details): Boolean {
        return (
            (prefs.isContactsChecked && checkContacts(number)) ||
            (prefs.isContactedChecked && checkContacted(number)) ||
            (prefs.isGroupsChecked && checkGroups(number)) ||
            (prefs.isRepeatedChecked && checkRepeated(number, callDetails)) ||
            (prefs.isMessagesChecked && checkMessages(number))
        )
    }

    private fun checkContacted(number: PhoneNumber): Boolean {
        val contacted = prefs.contacted
        var result = false
        for (value in Contact.entries.asSequence().filter { contacted.and(it.value) != 0 }) {
            result = when (value) {
                Contact.CALL -> checkContactedCall(number)
                Contact.MESSAGE -> checkContactedMessage(number)
                Contact.ANSWER -> checkContactedAnswer(number)
            }
            if (result) break
        }
        return result
    }

    private fun checkContactedCall(number: PhoneNumber): Boolean {
        var cursor: Cursor? = null
        var result = false
        try {
            cursor = ctx.contentResolver.query(
                makeContentUri(CallLog.Calls.CONTENT_FILTER_URI, number),
                arrayOf(CallLog.Calls._ID),
                "${CallLog.Calls.TYPE} = ${CallLog.Calls.OUTGOING_TYPE}",
                null,
                null,
            )
        } catch (_: SecurityException) {}
        cursor?.apply {
            if (moveToFirst()) result = true
            close()
        }
        return result
    }

    private fun checkContactedMessage(number: PhoneNumber): Boolean {
        var cursor: Cursor? = null
        var result = false
        val formatedNumber = phoneNumberUtil.format(
            number,
            PhoneNumberUtil.PhoneNumberFormat.E164,
        )
        var selection = "${Telephony.Sms.ADDRESS} = ?"
        var selectionArgs: Array<String>? = arrayOf(formatedNumber)
        if (isRequireQueryFix()) {
            selection = selection.replace("?", formatedNumber)
            selectionArgs = null
        }
        try {
            cursor = ctx.contentResolver.query(
                Telephony.Sms.Sent.CONTENT_URI,
                arrayOf(Telephony.Sms._ID),
                selection,
                selectionArgs,
                null,
            )
        } catch (_: SecurityException) {}
        cursor?.apply {
            if (moveToFirst()) result = true
            close()
        }
        return result
    }

    private fun checkContactedAnswer(number: PhoneNumber): Boolean {
        var cursor: Cursor? = null
        var result = false
        try {
            cursor = ctx.contentResolver.query(
                makeContentUri(CallLog.Calls.CONTENT_FILTER_URI, number),
                arrayOf(CallLog.Calls._ID),
                "${CallLog.Calls.TYPE} IN (${CallLog.Calls.INCOMING_TYPE}, " +
                        "${CallLog.Calls.ANSWERED_EXTERNALLY_TYPE})",
                null,
                null,
            )
        } catch (_: SecurityException) {}
        cursor?.apply {
            if (moveToFirst()) result = true
            close()
        }
        return result
    }

    private fun checkGroups(number: PhoneNumber): Boolean {
        val groups = prefs.groups
        var result = false
        val isLocal by lazy { phoneNumberUtil.isValidNumberForRegion(
            number,
            telephonyManager?.networkCountryIso?.uppercase(),
        ) }
        val numberType by lazy { phoneNumberUtil.getNumberType(number) }
        val isMobile by lazy { numberType == PhoneNumberUtil.PhoneNumberType.MOBILE }
        for (group in Group.entries.asSequence().filter { groups.and(it.value) != 0 }) {
            result = when (group) {
                Group.TOLL_FREE -> numberType == PhoneNumberUtil.PhoneNumberType.TOLL_FREE
                Group.MOBILE -> isMobile
                Group.LOCAL -> isLocal
                Group.NOT_LOCAL -> !isLocal
                Group.LOCAL_MOBILE -> isLocal && isMobile
            }
            if (result) break
        }
        return result
    }

    private fun checkRepeated(number: PhoneNumber, callDetails: Call.Details): Boolean {
        val cursor: Cursor?
        val type = CallLog.Calls.TYPE
        val dateLimit = (System.currentTimeMillis() - prefs.repeatedMinutes * 60 * 1000).toString()
        var selection = "($type = ${CallLog.Calls.BLOCKED_TYPE} OR " +
                "$type = ${CallLog.Calls.MISSED_TYPE} OR " +
                "$type = ${CallLog.Calls.REJECTED_TYPE}) AND " +
                "${CallLog.Calls.DATE} > ?"
        var selectionArgs: Array<String>? = arrayOf(dateLimit)
        if (isRequireQueryFix()) {
            selection = selection.replace("?", dateLimit)
            selectionArgs = null
        }
        try {
            cursor = ctx.contentResolver.query(
                makeContentUri(CallLog.Calls.CONTENT_FILTER_URI, number),
                arrayOf(CallLog.Calls._ID, CallLog.Calls.DATE),
                selection,
                selectionArgs,
                CallLog.Calls.DEFAULT_SORT_ORDER,
            )
        } catch (_: SecurityException) { return false }
        var result = false
        cursor?.apply {
            val i: Int
            val required = prefs.repeatedCount - 1
            val burstTimeout = prefs.repeatedBurstTimeout * 1000L
            if (burstTimeout == 0L) {
                i = count
            } else {
                var j = 0
                var tm = callDetails.creationTimeMillis
                while (moveToNext()) {
                    val date = getLong(getColumnIndexOrThrow(CallLog.Calls.DATE))
                    if (tm - date >= burstTimeout) {
                        j++
                        if (j >= required) break
                    }
                    tm = date
                }
                i = j
            }
            if (i >= required) result = true
            close()
        }
        return result
    }

    private fun checkMessages(number: PhoneNumber): Boolean {
        val messages = prefs.messages
        var result = false
        for (value in Message.entries.asSequence().filter { messages.and(it.value) != 0 }) {
            result = when (value) {
                Message.INBOX -> checkMessagesInbox(number)
                Message.TEXT -> checkMessagesText(number)
            }
            if (result) break
        }
        return result
    }

    private fun checkMessagesInbox(number: PhoneNumber): Boolean {
        if (phoneNumberUtil.getNumberType(number) != PhoneNumberUtil.PhoneNumberType.MOBILE)
            return false
        var cursor: Cursor? = null
        var result = false
        val formatedNumber = phoneNumberUtil.format(
            number,
            PhoneNumberUtil.PhoneNumberFormat.E164,
        )
        var selection = "${Telephony.Sms.ADDRESS} = ?"
        var selectionArgs: Array<String>? = arrayOf(formatedNumber)
        if (isRequireQueryFix()) {
            selection = selection.replace("?", formatedNumber)
            selectionArgs = null
        }
        try {
            cursor = ctx.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms._ID),
                selection,
                selectionArgs,
                null,
            )
        } catch (_: SecurityException) {}
        cursor?.apply {
            if (moveToFirst()) result = true
            close()
        }
        return result
    }

    private fun checkMessagesText(number: PhoneNumber): Boolean {
        val logNumber = PhoneNumber()
        val countryCode = telephonyManager?.networkCountryIso?.uppercase()
        for (row in db.selectActive()) {
            phoneNumberUtil.parseAndKeepRawInput(row.phoneNumber, countryCode, logNumber)
            if (phoneNumberUtil.isNumberMatch(number, logNumber) ==
                PhoneNumberUtil.MatchType.EXACT_MATCH) return true
        }
        return false
    }

    private fun checkContacts(number: PhoneNumber): Boolean {
        val cursor: Cursor?
        try {
            cursor = ctx.contentResolver.query(
                makeContentUri(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, number),
                arrayOf(ContactsContract.PhoneLookup._ID),
                null,
                null,
                null,
            )
        } catch (_: SecurityException) { return false }
        var result = false
        cursor?.apply {
            if (moveToFirst()) result = true
            close()
        }
        return result
    }

    private fun makeContentUri(base: Uri, number: PhoneNumber) =
        Uri.withAppendedPath(
            base,
            Uri.encode(phoneNumberUtil.format(
                number,
                PhoneNumberUtil.PhoneNumberFormat.E164,
            )),
        )

    private fun isRequireQueryFix() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 &&
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU
}