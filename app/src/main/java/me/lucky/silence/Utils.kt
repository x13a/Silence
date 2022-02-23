package me.lucky.silence

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

class Utils {
    companion object {
        fun setSmsReceiverState(ctx: Context, value: Boolean) {
            setComponentState(ctx, SmsReceiver::class.java, value)
        }

        fun setComponentState(ctx: Context, cls: Class<*>, value: Boolean) {
            ctx.packageManager.setComponentEnabledSetting(
                ComponentName(ctx, cls),
                if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )
        }

        fun getComponentState(ctx: Context, cls: Class<*>): Boolean {
            return ctx.packageManager.getComponentEnabledSetting(ComponentName(ctx, cls)) ==
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }

        fun hasCallScreeningRole(ctx: Context): Boolean {
            return ctx
                .getSystemService(RoleManager::class.java)
                ?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
        }

        fun hasPermissions(ctx: Context, vararg permissions: String): Boolean {
            return !permissions.any {
                ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
            }
        }

        fun getModemCount(ctx: Context): Int {
            val telephonyManager = ctx.getSystemService(TelephonyManager::class.java)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                telephonyManager?.supportedModemCount
            } else {
                @Suppress("deprecation")
                telephonyManager?.phoneCount
            } ?: 0
        }

        fun currentTimeSeconds() = System.currentTimeMillis() / 1000
    }
}
