package me.lucky.silence

import android.database.Cursor
import android.provider.CallLog
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.TelephonyManager

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

class CallScreeningService : CallScreeningService() {
    private val telephonyManager by lazy { getSystemService(TelephonyManager::class.java) }
    private val prefs by lazy { Preferences(this) }

    override fun onScreenCall(callDetails: Call.Details) {
        if (
            callDetails.callDirection != Call.Details.DIRECTION_INCOMING ||
            !prefs.isServiceEnabled
        ) {
            respondAllow(callDetails)
            return
        }
        val countryCode = telephonyManager?.networkCountryIso?.uppercase()
        val number: Phonenumber.PhoneNumber
        try {
            number = PhoneNumberUtil.getInstance().parse(
                callDetails.handle.schemeSpecificPart,
                countryCode,
            )
        } catch (exc: NumberParseException) {
            respondReject(callDetails)
            return
        }
        if (
            (prefs.isCallbackChecked && checkCallback(number, countryCode)) ||
            (prefs.isTollFreeChecked && checkTollFree(number))
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

    private fun checkCallback(number: Phonenumber.PhoneNumber, countryCode: String?): Boolean {
        val cursor: Cursor?
        try {
            cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(CallLog.Calls.NUMBER),
                "${CallLog.Calls.TYPE} = ?",
                arrayOf(CallLog.Calls.OUTGOING_TYPE.toString()),
                null,
            )
        } catch (exc: SecurityException) {
            return false
        }
        var result = false
        cursor?.apply {
            val phoneNumberUtil = PhoneNumberUtil.getInstance()
            while (moveToNext()) {
                val logNumber: Phonenumber.PhoneNumber
                try {
                    logNumber = phoneNumberUtil.parse(getString(0), countryCode)
                } catch (exc: NumberParseException) {
                    continue
                }
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
        return (PhoneNumberUtil.getInstance().getNumberType(number) ==
                PhoneNumberUtil.PhoneNumberType.TOLL_FREE)
    }
}
