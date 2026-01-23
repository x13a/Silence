package me.lucky.silence

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.provider.ContactsContract
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person

class NotificationManager(private val ctx: Context) {
    companion object {
        private const val CHANNEL_BLOCKED_CALLS_ID = "blocked_calls"
        private const val GROUP_BLOCKED_CALLS_KEY = "blocked"
    }

    private val manager = NotificationManagerCompat.from(ctx)

    fun createNotificationChannels() {
        manager.createNotificationChannel(NotificationChannelCompat.Builder(
            CHANNEL_BLOCKED_CALLS_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
        ).setName(ctx.getString(R.string.notification_channel)).build())
    }

    private fun getContactName(phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor = try {
            ctx.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null
            )
        } catch (_: SecurityException) {
            null
        }
        
        var contactName: String? = null
        cursor?.use {
            if (it.moveToFirst()) {
                contactName = it.getString(
                    it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
                )
            }
        }
        return contactName
    }

    fun notifyBlockedCall(tel: String, sim: Sim?) {
        var title = ctx.getString(R.string.notification_title)
        if (sim != null) title = "$title (${sim.name.replace('_', ' ')})"
        
        val contactName = getContactName(tel)
        val displayText = contactName ?: tel
        
        try {
            manager.notify(
                SystemClock.uptimeMillis().toInt(),
                NotificationCompat.Builder(ctx, CHANNEL_BLOCKED_CALLS_ID)
                    .setSmallIcon(R.drawable.ic_tile)
                    .setContentTitle(title)
                    .setContentText(displayText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setShowWhen(true)
                    .setAutoCancel(true)
                    .addPerson(Person.Builder().setUri("tel:$tel").build())
                    .setGroup(GROUP_BLOCKED_CALLS_KEY)
                    .setGroupSummary(true)
                    .build(),
            )
        } catch (_: SecurityException) {}
    }
}