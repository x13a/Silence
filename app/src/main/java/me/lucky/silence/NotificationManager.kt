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
    // The second parameter is the service center address, null means use the current default SMSC
    // The third parameter is the message to send
    // The fourth and fifth parameters are PendingIntent objects to be broadcast when the message is sent and delivered, respectively. We don't need these, so we set them to null
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }

// Function to send an SMS to a blocked call
    fun smsBlockedCall(tel: String, sim: Sim?) {
    // Send an SMS to the blocked number so the caller knows they have been blocked.
        sendSMS(tel, "Your number has been blocked because it is not known to the person you tried to call. If you want to contact this person, please send a message with your name and reason first.")
    }



}