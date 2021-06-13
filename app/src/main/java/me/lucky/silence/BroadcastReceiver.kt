package me.lucky.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val TOGGLE_ON = "me.lucky.silence.action.TOGGLE_ON"
        private const val TOGGLE_OFF = "me.lucky.silence.action.TOGGLE_OFF"
    }

    override fun onReceive(context: Context, intent: Intent) {
        with (Preferences(context)) {
            when (intent.action) {
                TOGGLE_ON -> isServiceEnabled = true
                TOGGLE_OFF -> isServiceEnabled = false
            }
        }
    }
}
