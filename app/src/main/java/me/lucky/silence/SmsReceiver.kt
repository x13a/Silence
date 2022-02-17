package me.lucky.silence

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
import java.util.concurrent.TimeUnit

import com.google.i18n.phonenumbers.PhoneNumberUtil

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
            val db by lazy { AppDatabase.getInstance(ctx).tmpNumberDao() }
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
                    .map { TmpNumber(it) }
                ) {
                    try {
                        db.insert(number)
                    } catch (exc: SQLiteConstraintException) {
                        db.update(number)
                    }
                    hasNumber = true
                }
            }
            if (hasNumber) schedule()
            pendingResult.finish()
        }

        private fun schedule() {
            ctx.getSystemService(JobScheduler::class.java)?.schedule(
                JobInfo.Builder(
                    CleanupJobService.JOB_ID,
                    ComponentName(ctx, CleanupJobService::class.java),
                )
                    .setMinimumLatency(TimeUnit
                        .SECONDS
                        .toMillis(TmpNumberDao.INACTIVE_DURATION.toLong() + 1))
                    .setPersisted(true)
                    .build()
            )
        }
    }
}
