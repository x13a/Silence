package me.lucky.silence.screening

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.text.isDigitsOnly
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import me.lucky.silence.*
import kotlin.properties.Delegates

class CallScreeningService : CallScreeningService() {
    private lateinit var prefs: Preferences
    private lateinit var callScreeningHelper: CallScreeningHelper
    private lateinit var phoneNumberUtil: PhoneNumberUtil
    private val notificationManager by lazy { NotificationManager(this) }
    private var isMultiSim by Delegates.notNull<Boolean>()
    private var telephonyManager: TelephonyManager? = null
    private var subscriptionManager: SubscriptionManager? = null

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        prefs = Preferences(this)
        callScreeningHelper = CallScreeningHelper(this)
        phoneNumberUtil = PhoneNumberUtil.getInstance()
        isMultiSim = Utils.getModemCount(this) >= 2
        telephonyManager = getSystemService(TelephonyManager::class.java)
        subscriptionManager = getSystemService(SubscriptionManager::class.java)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        if (!prefs.isEnabled) {
            respondAllow(callDetails)
            return
        } else if (checkEmergency(callDetails)) {
            prefs.isEnabled = false
            Utils.setMessagesTextEnabled(this, false)
            respondAllow(callDetails)
            return
        } else if (
            callDetails.callDirection != Call.Details.DIRECTION_INCOMING ||
            (prefs.isStirChecked && checkStir(callDetails)) ||
            (prefs.isUnknownNumbersChecked && checkUnknownNumber(callDetails)) ||
            (prefs.isShortNumbersChecked && checkShortNumber(callDetails)) ||
            checkSim()
        ) {
            respondAllow(callDetails)
            return
        }
        val number: Phonenumber.PhoneNumber
        try {
            number = phoneNumberUtil.parse(
                callDetails.handle?.schemeSpecificPart,
                telephonyManager?.networkCountryIso?.uppercase(),
            )
        } catch (exc: NumberParseException) {
            respondNotAllow(callDetails)
            return
        }
        if (callScreeningHelper.check(number)) respondAllow(callDetails)
        else respondNotAllow(callDetails)
    }

    private fun respondAllow(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())
    }

    private fun respondNotAllow(callDetails: Call.Details) {
        val responseOptions = prefs.responseOptions
        val disallowCall = responseOptions.and(ResponseOption.DisallowCall.value) != 0
        val tel = callDetails.handle?.schemeSpecificPart
        val isNotify = responseOptions.and(ResponseOption.SkipNotification.value) == 0
                && tel != null
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(disallowCall)
                .setRejectCall(responseOptions.and(ResponseOption.RejectCall.value) != 0
                        && disallowCall)
                .setSilenceCall(responseOptions.and(ResponseOption.SilenceCall.value) != 0)
                .setSkipCallLog(responseOptions.and(ResponseOption.SkipCallLog.value) != 0
                        && disallowCall)
                .setSkipNotification(!isNotify && disallowCall)
                .build(),
        )
        if (isNotify && disallowCall) notificationManager.notifyBlockedCall(tel)
    }

    private fun checkStir(callDetails: Call.Details): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return callDetails.callerNumberVerificationStatus ==
                    Connection.VERIFICATION_STATUS_PASSED
        }
        return false
    }

    private fun checkEmergency(callDetails: Call.Details): Boolean {
        return callDetails.hasProperty(Call.Details.PROPERTY_EMERGENCY_CALLBACK_MODE) ||
            callDetails.hasProperty(Call.Details.PROPERTY_NETWORK_IDENTIFIED_EMERGENCY_CALL) ||
            telephonyManager
                ?.isEmergencyNumber(callDetails.handle?.schemeSpecificPart ?: "") == true
    }

    private fun checkSim(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !isMultiSim) return false
        val sim = prefs.sim
        return (sim.and(Sim.SIM_1.value) != 0 && checkSimSlot(0)) ||
                (sim.and(Sim.SIM_2.value) != 0 && checkSimSlot(1))
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkSimSlot(slotIndex: Int): Boolean {
        return try {
            telephonyManager
                ?.createForSubscriptionId(subscriptionManager
                    ?.getActiveSubscriptionInfoForSimSlotIndex(slotIndex)
                    ?.subscriptionId ?: return false)
                ?.callStateForSubscription == TelephonyManager.CALL_STATE_RINGING
        } catch (exc: SecurityException) { false }
    }

    private fun checkUnknownNumber(callDetails: Call.Details) =
        callDetails.handle?.schemeSpecificPart == null

    private fun checkShortNumber(callDetails: Call.Details): Boolean {
        val v = callDetails.handle?.schemeSpecificPart?.trimStart('+') ?: return false
        return v.length in 3..5 && v.isDigitsOnly()
    }
}