package me.lucky.silence

import android.telecom.Call
import android.telecom.CallScreeningService

class CallScreeningService : CallScreeningService() {
    private val prefs by lazy { Preferences(this) }

    override fun onScreenCall(callDetails: Call.Details) {
        if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }
        val isEnabled = prefs.isServiceEnabled
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(isEnabled)
                .setRejectCall(isEnabled)
                .build(),
        )
    }
}
