package me.lucky.silence

import android.app.role.RoleManager
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
                TOGGLE_ON -> {
                    if (
                        context.getSystemService(RoleManager::class.java)
                            .isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
                    ) {
                        isServiceEnabled = true
                    }
                }
                TOGGLE_OFF -> isServiceEnabled = false
            }
        }
    }
}
