package me.lucky.silence.text

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.provider.Telephony
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import me.lucky.silence.AllowNumber
import me.lucky.silence.AppDatabase
import me.lucky.silence.Preferences
import java.util.concurrent.TimeUnit

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        Thread(Runner(context ?: return, intent, goAsync())).start()
    }

    private class Runner(
        private val ctx: Context,
        private val intent: Intent,
        private val pendingResult: PendingResult,
    ) : Runnable {
        private val phoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }
        private val telephonyManager by lazy { ctx.getSystemService(TelephonyManager::class.java) }

        override fun run() {
            val countryCode by lazy {
                telephonyManager?.networkCountryIso?.uppercase()
            }
            val db by lazy { AppDatabase.getInstance(ctx).allowNumberDao() }
            val prefs by lazy { Preferences(ctx) }
            var hasNumber = false
            for (msg in Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return) {
                if (
                    msg.isStatusReportMessage ||
                    msg.isCphsMwiMessage ||
                    msg.isMWIClearMessage ||
                    msg.isMWISetMessage ||
                    msg.isMwiDontStore ||
                    msg.isReplace ||
                    msg.originatingAddress == null ||
                    (
                        msg.messageClass != SmsMessage.MessageClass.CLASS_1
                        && msg.messageClass != SmsMessage.MessageClass.UNKNOWN
                    )
                ) continue
                for (number in phoneNumberUtil
                    .findNumbers(msg.messageBody, countryCode)
                    .asSequence()
                    .map { it.number() }
                    .filter {
                        phoneNumberUtil.getNumberType(it) == PhoneNumberUtil.PhoneNumberType.MOBILE
                    }
                    .map { AllowNumber.new(it, prefs.messagesTtl) }
                ) {
                    try {
                        db.insert(number)
                    } catch (_: SQLiteConstraintException) {
                        db.update(number)
                    }
                    hasNumber = true
                }
            }
            if (hasNumber) scheduleCleanup(ctx, prefs)
            pendingResult.finish()
        }

        private fun scheduleCleanup(ctx: Context, prefs: Preferences) =
            WorkManager
                .getInstance(ctx)
                .enqueue(OneTimeWorkRequestBuilder<CleanupWorker>()
                    .setInitialDelay(prefs.messagesTtl.toLong() + 5, TimeUnit.MINUTES)
                    .build())
    }
}