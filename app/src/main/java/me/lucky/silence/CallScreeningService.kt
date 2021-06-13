package me.lucky.silence

import android.provider.CallLog
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.PhoneNumberUtils

class CallScreeningService : CallScreeningService() {
    private val prefs by lazy { Preferences(this) }

    override fun onScreenCall(callDetails: Call.Details) {
        if (
            callDetails.callDirection != Call.Details.DIRECTION_INCOMING ||
            !prefs.isServiceEnabled
        ) {
            respondAllow(callDetails)
            return
        }
        if (prefs.isCallbackChecked) {
            try {
                val cursor = contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(CallLog.Calls.NUMBER),
                    "${CallLog.Calls.TYPE} = ?",
                    arrayOf(CallLog.Calls.OUTGOING_TYPE.toString()),
                    null,
                )
                cursor?.apply {
                    val number = callDetails.handle.schemeSpecificPart
                    var isFound = false
                    while (moveToNext()) {
                        if (PhoneNumberUtils.compare(number, getString(0))) {
                            isFound = true
                            break
                        }
                    }
                    close()
                    if (isFound) {
                        respondAllow(callDetails)
                        return
                    }
                }
            } catch (exc: SecurityException) {}
        }
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .build(),
        )
    }

    private fun respondAllow(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())
    }
}
