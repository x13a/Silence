package me.lucky.silence

import android.Manifest
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.telephony.TelephonyManager

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

class CallScreeningService : CallScreeningService() {
    companion object {
        private const val REPEATED_DURATION = 5 * 60 * 1000
    }

    private val telephonyManager by lazy { getSystemService(TelephonyManager::class.java) }
    private val prefs by lazy { Preferences(this) }
    private val phoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }
    private val db by lazy { AppDatabase.getInstance(this).tmpNumberDao() }

    override fun onScreenCall(callDetails: Call.Details) {
        if (
            callDetails.callDirection != Call.Details.DIRECTION_INCOMING ||
            !prefs.isServiceEnabled ||
            (prefs.isStirChecked && checkStir(callDetails))
        ) {
            respondAllow(callDetails)
            return
        }
        val number: Phonenumber.PhoneNumber
        try {
            number = phoneNumberUtil.parse(
                callDetails.handle.schemeSpecificPart,
                telephonyManager?.networkCountryIso?.uppercase(),
            )
        } catch (exc: NumberParseException) {
            respondReject(callDetails)
            return
        }
        if (
            (hasContactsPermission() && checkContacts(number)) ||
            (prefs.isCallbackChecked && checkCallback(number)) ||
            (prefs.isCodeChecked && checkCode(number)) ||
            (prefs.isRepeatedChecked && checkRepeated(number)) ||
            (prefs.isMessageChecked && checkMessage(number))
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

    private fun checkCallback(number: Phonenumber.PhoneNumber): Boolean {
        val cursor: Cursor?
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
        return result
    }

    private fun checkCode(number: Phonenumber.PhoneNumber): Boolean {
        return phoneNumberUtil.getNumberType(number) == PhoneNumberUtil.PhoneNumberType.TOLL_FREE
    }

    private fun checkRepeated(number: Phonenumber.PhoneNumber): Boolean {
        val cursor: Cursor?
        try {
            cursor = contentResolver.query(
                makeContentUri(CallLog.Calls.CONTENT_FILTER_URI, number),
                arrayOf(CallLog.Calls._ID),
                "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.DATE} > ?",
                arrayOf(
                    CallLog.Calls.BLOCKED_TYPE.toString(),
                    (System.currentTimeMillis() - REPEATED_DURATION).toString(),
                ),
                null,
            )
        } catch (exc: SecurityException) { return false }
        var result = false
        cursor?.apply {
            if (count >= 2) { result = true }
            close()
        }
        return result
    }
    
    private fun checkMessage(number: Phonenumber.PhoneNumber): Boolean {
        val logNumber = Phonenumber.PhoneNumber()
        val countryCode = telephonyManager?.networkCountryIso?.uppercase()
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
        return Utils.hasPermission(this, Manifest.permission.READ_CONTACTS)
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
