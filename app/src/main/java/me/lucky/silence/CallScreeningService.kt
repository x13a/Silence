package me.lucky.silence

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import kotlin.properties.Delegates

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

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
        if (!prefs.isServiceEnabled) {
            respondAllow(callDetails)
            return
        } else if (checkEmergency(callDetails)) {
            prefs.isServiceEnabled = false
            Utils.setSmsReceiverState(this, false)
            respondAllow(callDetails)
            return
        } else if (
            callDetails.callDirection != Call.Details.DIRECTION_INCOMING ||
            (prefs.isStirChecked && checkStir(callDetails)) ||
            checkUnknownNumber(callDetails) ||
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
            respondReject(callDetails)
            return
        }
        if (callScreeningHelper.check(number) == CallScreeningHelper.RESULT_ALLOW)
            respondAllow(callDetails) else respondReject(callDetails)
    }

    private fun respondAllow(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())
    }

    private fun respondReject(callDetails: Call.Details) {
        val isNotify = prefs.isGeneralNotificationsChecked
        val tel = callDetails.handle?.schemeSpecificPart
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipNotification(!isNotify || tel == null)
                .build(),
        )
        if (isNotify) notificationManager.notifyBlockedCall(tel)
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

    private fun checkUnknownNumber(callDetails: Call.Details): Boolean {
        return callDetails.handle?.schemeSpecificPart == null &&
                prefs.isGeneralUnknownNumbersChecked
    }
}
