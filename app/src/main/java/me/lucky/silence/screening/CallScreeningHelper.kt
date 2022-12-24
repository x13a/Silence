package me.lucky.silence.screening

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import android.telecom.Call
import android.telephony.TelephonyManager

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import me.lucky.silence.*

class CallScreeningHelper(private val ctx: Context) {
    private val telephonyManager = ctx.getSystemService(TelephonyManager::class.java)
    private val prefs = Preferences(ctx)
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    private val db = AppDatabase.getInstance(ctx).allowNumberDao()

    fun check(number: Phonenumber.PhoneNumber, callDetails: Call.Details): Boolean {
        return (
            (prefs.isContactsChecked && checkContacts(number)) ||
            (prefs.isContactedChecked && checkContacted(number)) ||
            (prefs.isGroupsChecked && checkGroups(number)) ||
            (prefs.isRepeatedChecked && checkRepeated(number, callDetails)) ||
            (prefs.isMessagesChecked && checkMessages(number))
        )
    }

    private fun checkContacted(number: Phonenumber.PhoneNumber): Boolean {
        val contacted = prefs.contacted
        var result = false
        for (value in Contact.values().asSequence().filter { contacted.and(it.value) != 0 }) {
            result = when (value) {
                Contact.CALL -> checkContactedCall(number)
                Contact.MESSAGE -> checkContactedMessage(number)
            }
            if (result) break
        }
        return result
    }

    private fun checkContactedCall(number: Phonenumber.PhoneNumber): Boolean {
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
        } catch (exc: SecurityException) {}
        cursor?.apply {
            if (moveToFirst()) result = true
            close()
        }
        return result
    }

    private fun checkContactedMessage(number: Phonenumber.PhoneNumber): Boolean {
        var cursor: Cursor? = null
        var result = false
        try {
            cursor = ctx.contentResolver.query(
                Telephony.Sms.Sent.CONTENT_URI,
                arrayOf(Telephony.Sms._ID),
                "${Telephony.Sms.ADDRESS} = " + phoneNumberUtil.format(
                    number,
                    PhoneNumberUtil.PhoneNumberFormat.E164,
                ),
                arrayOf(),
                null,
            )
        } catch (exc: SecurityException) {}
        cursor?.apply {
            if (moveToFirst()) result = true
            close()
        }
        return result
    }

    private fun checkGroups(number: Phonenumber.PhoneNumber): Boolean {
        val groups = prefs.groups
        var result = false
        val isLocal by lazy { phoneNumberUtil.isValidNumberForRegion(
            number,
            telephonyManager?.networkCountryIso?.uppercase(),
        ) }
        val numberType by lazy { phoneNumberUtil.getNumberType(number) }
        val isMobile by lazy { numberType == PhoneNumberUtil.PhoneNumberType.MOBILE }
        for (group in Group.values().asSequence().filter { groups.and(it.value) != 0 }) {
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

    private fun checkRepeated(number: Phonenumber.PhoneNumber, callDetails: Call.Details): Boolean {
        val cursor: Cursor?
        try {
            cursor = ctx.contentResolver.query(
                makeContentUri(CallLog.Calls.CONTENT_FILTER_URI, number),
                arrayOf(CallLog.Calls._ID, CallLog.Calls.DATE),
                "${CallLog.Calls.TYPE} = ${CallLog.Calls.BLOCKED_TYPE} AND ${CallLog.Calls.DATE} > " +
                        (System.currentTimeMillis() - prefs.repeatedMinutes * 60 * 1000).toString(),
                null,
                CallLog.Calls.DEFAULT_SORT_ORDER,
            )
        } catch (exc: SecurityException) { return false }
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

    private fun checkMessages(number: Phonenumber.PhoneNumber): Boolean {
        val messages = prefs.messages
        var result = false
        for (value in Message.values().asSequence().filter { messages.and(it.value) != 0 }) {
            result = when (value) {
                Message.INBOX -> checkMessagesInbox(number)
                Message.TEXT -> checkMessagesText(number)
            }
            if (result) break
        }
        return result
    }

    private fun checkMessagesInbox(number: Phonenumber.PhoneNumber): Boolean {
        if (phoneNumberUtil.getNumberType(number) != PhoneNumberUtil.PhoneNumberType.MOBILE)
            return false
        var cursor: Cursor? = null
        var result = false
        try {
            cursor = ctx.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(Telephony.Sms._ID),
                "${Telephony.Sms.ADDRESS} = " + phoneNumberUtil.format(
                    number,
                    PhoneNumberUtil.PhoneNumberFormat.E164,
                ),
                null,
                null,
            )
        } catch (exc: SecurityException) {}
        cursor?.apply {
            if (moveToFirst()) result = true
            close()
        }
        return result
    }

    private fun checkMessagesText(number: Phonenumber.PhoneNumber): Boolean {
        val logNumber = Phonenumber.PhoneNumber()
        val countryCode = telephonyManager?.networkCountryIso?.uppercase()
        for (row in db.selectActive()) {
            phoneNumberUtil.parseAndKeepRawInput(row.phoneNumber, countryCode, logNumber)
            if (phoneNumberUtil.isNumberMatch(number, logNumber) ==
                PhoneNumberUtil.MatchType.EXACT_MATCH) return true
        }
        return false
    }

    private fun checkContacts(number: Phonenumber.PhoneNumber): Boolean {
        val cursor: Cursor?
        try {
            cursor = ctx.contentResolver.query(
                makeContentUri(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, number),
                arrayOf(ContactsContract.PhoneLookup._ID),
                null,
                null,
                null,
            )
        } catch (exc: SecurityException) { return false }
        var result = false
        cursor?.apply {
            if (moveToFirst()) result = true
            close()
        }
        return result
    }

    private fun makeContentUri(base: Uri, number: Phonenumber.PhoneNumber) =
        Uri.withAppendedPath(
            base,
            Uri.encode(phoneNumberUtil.format(
                number,
                PhoneNumberUtil.PhoneNumberFormat.E164,
            )),
        )
}