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
    companion object {
        private const val JOB_ID = 1
    }

    private val phoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

    override fun onReceive(context: Context, intent: Intent) {
        val countryCode by lazy {
            context.getSystemService(TelephonyManager::class.java)?.networkCountryIso?.uppercase()
        }
        val db by lazy { AppDatabase.getInstance(context).tmpNumberDao() }
        var hasNumber = false
        for (msg in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            if (
                msg.isStatusReportMessage ||
                msg.isEmail ||
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
            ) {
                if (phoneNumberUtil.getNumberType(number) !=
                    PhoneNumberUtil.PhoneNumberType.MOBILE) continue
                val tmpNumber = TmpNumber(number)
                try {
                    db.insert(tmpNumber)
                } catch (exc: SQLiteConstraintException) {
                    db.update(tmpNumber)
                }
                hasNumber = true
            }
        }
        if (hasNumber) {
            val jobScheduler = context
                .getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(
                JobInfo.Builder(JOB_ID, ComponentName(context, CleanupJobService::class.java))
                    .setMinimumLatency(TimeUnit
                        .SECONDS
                        .toMillis(TmpNumberDao.INACTIVE_DURATION.toLong() + 1))
                    .build()
            )
        }
    }
}
