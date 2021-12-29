package me.lucky.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.provider.Telephony
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

import com.google.i18n.phonenumbers.PhoneNumberUtil

class SmsReceiver : BroadcastReceiver() {
    private val phoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

    override fun onReceive(context: Context, intent: Intent) {
        val countryCode by lazy {
            context.getSystemService(TelephonyManager::class.java)?.networkCountryIso?.uppercase()
        }
        val db by lazy { AppDatabase.getInstance(context).smsFilterDao() }
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
            val numbers = phoneNumberUtil.findNumbers(msg.messageBody, countryCode)
            if (numbers.count() != 1) continue
            val number = numbers.first().number()
            if (phoneNumberUtil.getNumberType(number) !=
                PhoneNumberUtil.PhoneNumberType.MOBILE) continue
            val smsFilter = SmsFilter
                .new(phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164))
            try {
                db.insert(smsFilter)
            } catch (exc: SQLiteConstraintException) {
                db.update(smsFilter)
            }
            hasNumber = true
        }
        if (hasNumber) {
            val cleanupRequest = OneTimeWorkRequestBuilder<CleanupWorker>()
                .setInitialDelay(SmsFilterDao.INACTIVE_DURATION.toLong(), TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(cleanupRequest)
        }
    }
}
