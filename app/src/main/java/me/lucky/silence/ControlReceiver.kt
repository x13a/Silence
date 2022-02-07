package me.lucky.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ControlReceiver : BroadcastReceiver() {
    companion object {
        private const val SET_ON = "me.lucky.silence.action.SET_ON"
        private const val SET_OFF = "me.lucky.silence.action.SET_OFF"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val isOn = when (intent.action) {
            SET_ON -> true
            SET_OFF -> false
            else -> return
        }
        Preferences(context).apply {
            isServiceEnabled = isOn
            Utils.setSmsReceiverState(context, isServiceEnabled && isMessagesChecked)
        }
    }
}
