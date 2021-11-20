package me.lucky.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ControlReceiver : BroadcastReceiver() {
    companion object {
        private const val TOGGLE_ON = "me.lucky.silence.action.TOGGLE_ON"
        private const val TOGGLE_OFF = "me.lucky.silence.action.TOGGLE_OFF"
        private const val DELETE_INACTIVE = "me.lucky.silence.DELETE_SMS_FILTER_INACTIVE"
        private const val DELETE_ALL = "me.lucky.silence.action.DELETE_SMS_FILTER_ALL"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isOn = when (intent.action) {
            TOGGLE_ON -> true
            TOGGLE_OFF -> false
            else -> {
                AppDatabase.getInstance(context).smsFilterDao().apply {
                    when (intent.action) {
                        DELETE_INACTIVE -> deleteInactive()
                        DELETE_ALL -> deleteAll()
                    }
                }
                return
            }
        }
        Preferences(context).apply {
            isServiceEnabled = isOn
            Utils.setSmsReceiverState(context, isServiceEnabled && isSmsChecked)
        }
    }
}
