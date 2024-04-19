package me.lucky.silence

import android.app.ActivityManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager

import me.lucky.silence.text.NotificationListenerService

class Utils {
    companion object {
        fun isNotificationListenerRunning(ctx: Context): Boolean {
            val manager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("deprecation")
            val services = manager.getRunningServices(Integer.MAX_VALUE)
            for (service in services) {
                if (NotificationListenerService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        fun setMessagesTextEnabled(ctx: Context, value: Boolean) {
            setComponentEnabled(ctx, NotificationListenerService::class.java, value)
            if (value && !isNotificationListenerRunning(ctx)) {
                ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

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

        fun getModemCount(ctx: Context, modem: Modem): Int {
            val telephonyManager = ctx.getSystemService(TelephonyManager::class.java)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                when (modem) {
                    Modem.ACTIVE -> telephonyManager?.activeModemCount
                    Modem.SUPPORTED -> telephonyManager?.supportedModemCount
                }
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

enum class Modem {
    ACTIVE,
    SUPPORTED,
}