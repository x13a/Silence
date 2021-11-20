package me.lucky.silence

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

class Utils {
    companion object {
        fun setSmsReceiverState(ctx: Context, value: Boolean) {
            ctx.packageManager.setComponentEnabledSetting(
                ComponentName(ctx, SmsReceiver::class.java),
                if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
        }

        fun hasCallScreeningRole(ctx: Context): Boolean {
            return ctx
                .getSystemService(RoleManager::class.java)
                .isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        }
    }
}
