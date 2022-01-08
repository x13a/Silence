package me.lucky.silence

import android.Manifest
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Telephony
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.telephony.TelephonyManager

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

class CallScreeningService : CallScreeningService() {
    private val telephonyManager by lazy { getSystemService(TelephonyManager::class.java) }
    private val prefs by lazy { Preferences(this) }
    private val phoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }
    private val db by lazy { AppDatabase.getInstance(this).tmpNumberDao() }

    override fun onScreenCall(callDetails: Call.Details) {
        if (!prefs.isServiceEnabled) {
            respondAllow(callDetails)
            return
        } else if (
            callDetails.hasProperty(Call.Details.PROPERTY_EMERGENCY_CALLBACK_MODE) ||
            callDetails.hasProperty(Call.Details.PROPERTY_NETWORK_IDENTIFIED_EMERGENCY_CALL) ||
            telephonyManager.isEmergencyNumber(callDetails.handle.schemeSpecificPart)
        ) {
            prefs.isServiceEnabled = false
            Utils.setSmsReceiverState(this, false)
            respondAllow(callDetails)
            return
        } else if (
            callDetails.callDirection != Call.Details.DIRECTION_INCOMING ||
            (prefs.isStirChecked && checkStir(callDetails))
        ) {
            respondAllow(callDetails)
            return
        }
        val number: Phonenumber.PhoneNumber
        try {
            number = phoneNumberUtil.parse(
                callDetails.handle.schemeSpecificPart,
                telephonyManager.networkCountryIso.uppercase(),
            )
        } catch (exc: NumberParseException) {
            respondAllow(callDetails)
            return
        }
        if (
            (hasContactsPermission() && checkContacts(number)) ||
            (prefs.isContactedChecked && checkContacted(number)) ||
            (prefs.isGroupsChecked && checkGroups(number)) ||
            (prefs.isRepeatedChecked && checkRepeated(number)) ||
            (prefs.isMessagesChecked && checkMessages(number))
        ) {
            respondAllow(callDetails)
            return
        }
        respondReject(callDetails)
    }

    private fun respondAllow(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())
    }

    private fun respondReject(callDetails: Call.Details) {
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .build(),
        )
    }

    private fun checkContacted(number: Phonenumber.PhoneNumber): Boolean {
        var cursor: Cursor?
        try {
            cursor = contentResolver.query(
                makeContentUri(CallLog.Calls.CONTENT_FILTER_URI, number),
                arrayOf(CallLog.Calls._ID),
                "${CallLog.Calls.TYPE} = ?",
                arrayOf(CallLog.Calls.OUTGOING_TYPE.toString()),
                null,
            )
        } catch (exc: SecurityException) { return false }
        var result = false
        cursor?.apply {
            if (moveToFirst()) { result = true }
            close()
        }
        if (result) return result
        try {
            cursor = contentResolver.query(
                Telephony.Sms.Sent.CONTENT_URI,
                arrayOf(Telephony.Sms._ID),
                "${Telephony.Sms.ADDRESS} = ?",
                arrayOf(phoneNumberUtil.format(
                    number,
                    PhoneNumberUtil.PhoneNumberFormat.E164,
                )),
                null,
            )
        } catch (exc: SecurityException) { return false }
        cursor?.apply {
            if (moveToFirst()) { result = true }
            close()
        }
        return result
    }

    private fun checkGroups(number: Phonenumber.PhoneNumber): Boolean {
        val groupsFlag = prefs.groupsFlag
        var result = false
        for (group in Group.values().asSequence().filter { groupsFlag.and(it.flag) != 0 }) {
            result = when (group) {
                Group.TOLL_FREE -> phoneNumberUtil.getNumberType(number) ==
                    PhoneNumberUtil.PhoneNumberType.TOLL_FREE
                Group.LOCAL -> phoneNumberUtil.isValidNumberForRegion(
                    number,
                    telephonyManager.networkCountryIso.uppercase(),
                )
            }
            if (result) break
        }
        return result
    }

    private fun checkRepeated(number: Phonenumber.PhoneNumber): Boolean {
        val cursor: Cursor?
        val repeatedSettings = prefs.repeatedSettings
        try {
            cursor = contentResolver.query(
                makeContentUri(CallLog.Calls.CONTENT_FILTER_URI, number),
                arrayOf(CallLog.Calls._ID),
                "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.DATE} > ?",
                arrayOf(
                    CallLog.Calls.BLOCKED_TYPE.toString(),
                    (System.currentTimeMillis() - repeatedSettings.minutes * 60 * 1000).toString(),
                ),
                null,
            )
        } catch (exc: SecurityException) { return false }
        var result = false
        cursor?.apply {
            if (count >= repeatedSettings.count - 1) { result = true }
            close()
        }
        return result
    }
    
    private fun checkMessages(number: Phonenumber.PhoneNumber): Boolean {
        val logNumber = Phonenumber.PhoneNumber()
        val countryCode = telephonyManager.networkCountryIso.uppercase()
        for (row in db.selectActive()) {
            phoneNumberUtil.parseAndKeepRawInput(row.phoneNumber, countryCode, logNumber)
            if (phoneNumberUtil.isNumberMatch(number, logNumber) ==
                PhoneNumberUtil.MatchType.EXACT_MATCH) return true
        }
        return false
    }

    private fun checkStir(callDetails: Call.Details): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (callDetails.callerNumberVerificationStatus ==
                Connection.VERIFICATION_STATUS_PASSED) return true
        }
        return false
    }

    private fun checkContacts(number: Phonenumber.PhoneNumber): Boolean {
        val cursor: Cursor?
        try {
            cursor = contentResolver.query(
                makeContentUri(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, number),
                arrayOf(ContactsContract.PhoneLookup._ID),
                null,
                null,
                null,
            )
        } catch (exc: SecurityException) { return false }
        var result = false
        cursor?.apply {
            if (moveToFirst()) { result = true }
            close()
        }
        return result
    }

    private fun hasContactsPermission(): Boolean {
        return Utils.hasPermissions(this, Manifest.permission.READ_CONTACTS)
    }

    private fun makeContentUri(base: Uri, number: Phonenumber.PhoneNumber): Uri {
        return Uri.withAppendedPath(
            base,
            Uri.encode(phoneNumberUtil.format(
                number,
                PhoneNumberUtil.PhoneNumberFormat.E164,
            )),
        )
    }
}
