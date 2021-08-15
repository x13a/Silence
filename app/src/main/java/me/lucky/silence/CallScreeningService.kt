package me.lucky.silence

import android.database.Cursor
import android.os.Build
import android.provider.CallLog
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
    private val db by lazy { AppDatabase.getInstance(this).smsFilterDao() }

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
            (prefs.isCallbackChecked && checkCallback(number)) ||
            (prefs.isTollFreeChecked && checkTollFree(number)) ||
            (prefs.isRepeatedChecked && checkRepeated(number)) ||
            (prefs.isSmsChecked && checkSms(number))
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
        // Pixel 4a, Android 11
        // redirect on reject not working correctly
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
            // Pixel 4a, Android 11
            // CallLog.Calls.CONTENT_FILTER_URI: Unknown URL content://call_log/calls/filter
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.COUNTRY_ISO),
                "${CallLog.Calls.TYPE} = ?",
                arrayOf(CallLog.Calls.OUTGOING_TYPE.toString()),
                null,
            )
        } catch (exc: SecurityException) { return false }
        var result = false
        cursor?.apply {
            val logNumber = Phonenumber.PhoneNumber()
            while (moveToNext()) {
                try {
                    phoneNumberUtil.parseAndKeepRawInput(
                        getString(0),
                        getString(1),
                        logNumber,
                    )
                } catch (exc: NumberParseException) { continue }
                if (
                    phoneNumberUtil.isNumberMatch(number, logNumber) ==
                    PhoneNumberUtil.MatchType.EXACT_MATCH
                ) {
                    result = true
                    break
                }
            }
            close()
        }
        return result
    }

    private fun checkTollFree(number: Phonenumber.PhoneNumber): Boolean {
        return phoneNumberUtil.getNumberType(number) == PhoneNumberUtil.PhoneNumberType.TOLL_FREE
    }

    private fun checkRepeated(number: Phonenumber.PhoneNumber): Boolean {
        val cursor: Cursor?
        try {
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.COUNTRY_ISO),
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
            var count = 0
            val logNumber = Phonenumber.PhoneNumber()
            while (moveToNext()) {
                try {
                    phoneNumberUtil.parseAndKeepRawInput(
                        getString(0),
                        getString(1),
                        logNumber,
                    )
                } catch (exc: NumberParseException) { continue }
                if (
                    phoneNumberUtil.isNumberMatch(number, logNumber) ==
                    PhoneNumberUtil.MatchType.EXACT_MATCH
                ) {
                    count++
                    if (count >= 2) {
                        result = true
                        break
                    }
                }
            }
            close()
        }
        return result
    }
    
    private fun checkSms(number: Phonenumber.PhoneNumber): Boolean {
        val logNumber = Phonenumber.PhoneNumber()
        val countryCode = telephonyManager?.networkCountryIso?.uppercase()
        for (filter in db.getActive()) {
            phoneNumberUtil.parseAndKeepRawInput(filter.phoneNumber, countryCode, logNumber)
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
}
