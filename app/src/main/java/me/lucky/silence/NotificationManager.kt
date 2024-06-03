package me.lucky.silence

import android.content.Context
import android.os.SystemClock
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import android.telephony.SmsManager


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

    fun notifyBlockedCall(tel: String, sim: Sim?) {
        var title = ctx.getString(R.string.notification_title)
        if (sim != null) title = "$title (${sim.name.replace('_', ' ')})"
        try {
            manager.notify(
                SystemClock.uptimeMillis().toInt(),
                NotificationCompat.Builder(ctx, CHANNEL_BLOCKED_CALLS_ID)
                    .setSmallIcon(R.drawable.ic_tile)
                    .setContentTitle(title)
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
        } catch (_: SecurityException) {}
    }


// Function to send an SMS message
    fun sendSMS(phoneNumber: String, message: String) {
    // Get the default instance of the SmsManager
    val smsManager = SmsManager.getDefault()
    // Send a text message to the provided phone number
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }

// Function to send an SMS to a blocked call
fun smsBlockedCall(tel: String, sim: Sim?) {
    // Get the SMS message from Preferences
    val prefs = Preferences(ctx)
    val message = prefs.smsMessage

    // If the message is not empty, send an SMS to the blocked number
    if (!message.isNullOrEmpty()) {
        sendSMS(tel, message)
    }



}}