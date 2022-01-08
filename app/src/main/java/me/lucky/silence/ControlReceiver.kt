package me.lucky.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ControlReceiver : BroadcastReceiver() {
    companion object {
        private const val TOGGLE_ON = "me.lucky.silence.action.TOGGLE_ON"
        private const val TOGGLE_OFF = "me.lucky.silence.action.TOGGLE_OFF"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isOn = when (intent.action) {
            TOGGLE_ON -> true
            TOGGLE_OFF -> false
            else -> return
        }
        Preferences(context).apply {
            isServiceEnabled = isOn
            Utils.setSmsReceiverState(context, isServiceEnabled && isMessagesChecked)
        }
    }
}
