package me.lucky.silence

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager

import me.lucky.silence.text.NotificationListenerService

class Utils {
    companion object {
        fun setMessagesTextEnabled(ctx: Context, value: Boolean) =
            setComponentEnabled(ctx, NotificationListenerService::class.java, value)

        fun updateMessagesTextEnabled(ctx: Context) {
            val prefs = Preferences(ctx)
            setMessagesTextEnabled(
                ctx,
                prefs.isEnabled &&
                        prefs.isMessagesChecked &&
                        prefs.messages.and(Message.TEXT.value) != 0,
            )
        }

        fun setComponentEnabled(ctx: Context, cls: Class<*>, value: Boolean) =
            ctx.packageManager.setComponentEnabledSetting(
                ComponentName(ctx, cls),
                if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP,
            )

        fun isComponentEnabled(ctx: Context, cls: Class<*>) =
            ctx.packageManager.getComponentEnabledSetting(ComponentName(ctx, cls)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        fun hasCallScreeningRole(ctx: Context) =
            ctx
                .getSystemService(RoleManager::class.java)
                ?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false

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

        fun setFlag(key: Int, value: Int, enabled: Boolean) =
            when(enabled) {
                true -> key.or(value)
                false -> key.and(value.inv())
            }
    }
}