package me.lucky.silence

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person

class NotificationManager(private val ctx: Context) {
    companion object {
        private const val CHANNEL_BLOCKED_CALLS_ID = "blocked_calls"
        private const val GROUP_BLOCKED_CALLS_KEY = "blocked"
        private const val NOTIFICATION_BLOCKED_CALLS_ID = 1000
    }

    private val manager = NotificationManagerCompat.from(ctx)

    fun createNotificationChannels() {
        manager.createNotificationChannel(NotificationChannelCompat.Builder(
            CHANNEL_BLOCKED_CALLS_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
        ).setName(ctx.getString(R.string.notification_channel_blocked_calls)).build())
    }

    fun notifyBlockedCall(tel: String?) {
        if (tel == null) return
        manager.notify(
            NOTIFICATION_BLOCKED_CALLS_ID,
            NotificationCompat.Builder(ctx, CHANNEL_BLOCKED_CALLS_ID)
                .setSmallIcon(R.drawable.ic_tile)
                .setContentTitle(ctx.getString(R.string.notification_blocked_call_title))
                .setContentText(tel)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setShowWhen(true)
                .setAutoCancel(true)
                .addPerson(Person.Builder().setUri("tel:$tel").build())
                .setGroup(GROUP_BLOCKED_CALLS_KEY)
                .setGroupSummary(true)
                .build(),
        )
    }
}
