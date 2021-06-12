package me.lucky.silence

import android.provider.CallLog
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telephony.PhoneNumberUtils

class CallScreeningService : CallScreeningService() {
    private val prefs by lazy { Preferences(this) }

    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondAllow(callDetails)
            return
        }
        val isEnabled = prefs.isServiceEnabled
        if (isEnabled && prefs.isCallbackChecked) {
            try {
                val cursor = contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(CallLog.Calls.NUMBER),
                    "${CallLog.Calls.TYPE} = ?",
                    arrayOf(CallLog.Calls.OUTGOING_TYPE.toString()),
                    null,
                )
                cursor?.apply {
                    var isFound = false
                    while (moveToNext()) {
                        if (PhoneNumberUtils.compare(
                            callDetails.handle.schemeSpecificPart,
                            getString(0),
                        )) {
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
                .setDisallowCall(isEnabled)
                .setRejectCall(isEnabled)
                .build(),
        )
    }

    private fun respondAllow(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())
    }
}
