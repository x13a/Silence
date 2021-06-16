package me.lucky.silence

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class ControlReceiver : BroadcastReceiver() {
    companion object {
        private const val TOGGLE_ON = "me.lucky.silence.action.TOGGLE_ON"
        private const val TOGGLE_OFF = "me.lucky.silence.action.TOGGLE_OFF"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Preferences(context).apply {
            isServiceEnabled = intent.action == TOGGLE_ON
            setSmsReceiverState(context, isServiceEnabled && isSmsChecked)
        }
    }

    private fun setSmsReceiverState(context: Context, value: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, SmsReceiver::class.java),
            if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
    }
}
