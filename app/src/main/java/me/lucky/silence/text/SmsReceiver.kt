package me.lucky.silence.text

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.provider.Telephony
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import me.lucky.silence.AllowNumber
import me.lucky.silence.AppDatabase
import me.lucky.silence.Preferences
import java.util.concurrent.TimeUnit

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Thread(Runner(context ?: return, intent ?: return, goAsync())).start()
    }

    private class Runner(
        private val ctx: Context,
        private val intent: Intent,
        private val pendingResult: PendingResult,
    ) : Runnable {
        private val phoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

        override fun run() {
            val countryCode by lazy {
                ctx.getSystemService(TelephonyManager::class.java)?.networkCountryIso?.uppercase()
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
                        msg.messageClass != SmsMessage.MessageClass.CLASS_1 &&
                        msg.messageClass != SmsMessage.MessageClass.UNKNOWN
                    )
                ) continue
                for (number in phoneNumberUtil
                    .findNumbers(msg.messageBody, countryCode)
                    .asSequence()
                    .map { it.number() }
                    .filter {
                        phoneNumberUtil.getNumberType(it) == PhoneNumberUtil.PhoneNumberType.MOBILE
                    }
                    .map { AllowNumber.new(it, prefs.messagesTextTtl) }
                ) {
                    try {
                        db.insert(number)
                    } catch (exc: SQLiteConstraintException) {
                        db.update(number)
                    }
                    hasNumber = true
                }
            }
            if (hasNumber) schedule(prefs)
            pendingResult.finish()
        }

        private fun schedule(prefs: Preferences) {
            ctx.getSystemService(JobScheduler::class.java)?.schedule(
                JobInfo.Builder(
                    CleanJobService.JOB_ID,
                    ComponentName(ctx, CleanJobService::class.java),
                )
                    .setMinimumLatency(TimeUnit
                        .MINUTES
                        .toMillis(prefs.messagesTextTtl.toLong() + 5))
                    .setPersisted(true)
                    .build()
            )
        }
    }
}