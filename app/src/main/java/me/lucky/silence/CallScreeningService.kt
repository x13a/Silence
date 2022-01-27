package me.lucky.silence

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.telephony.TelephonyManager

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

class CallScreeningService : CallScreeningService() {
    private lateinit var prefs: Preferences
    private lateinit var screeningHelper: ScreeningHelper
    private lateinit var phoneNumberUtil: PhoneNumberUtil
    private val notificationManager by lazy { NotificationManager(this) }
    private var telephonyManager: TelephonyManager? = null

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        prefs = Preferences(this)
        screeningHelper = ScreeningHelper(this)
        phoneNumberUtil = PhoneNumberUtil.getInstance()
        telephonyManager = getSystemService(TelephonyManager::class.java)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        if (!prefs.isServiceEnabled) {
            respondAllow(callDetails)
            return
        } else if (
            callDetails.hasProperty(Call.Details.PROPERTY_EMERGENCY_CALLBACK_MODE) ||
            callDetails.hasProperty(Call.Details.PROPERTY_NETWORK_IDENTIFIED_EMERGENCY_CALL) ||
            telephonyManager
                ?.isEmergencyNumber(callDetails.handle?.schemeSpecificPart ?: "") == true
        ) {
            prefs.isServiceEnabled = false
            Utils.setSmsReceiverState(this, false)
            respondAllow(callDetails)
            return
        } else if (
            callDetails.callDirection != Call.Details.DIRECTION_INCOMING ||
            (prefs.isStirChecked && checkStir(callDetails))
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
        if (screeningHelper.check(number) == ScreeningHelper.RESULT_ALLOW)
            respondAllow(callDetails) else respondReject(callDetails)
    }

    private fun respondAllow(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())
    }

    private fun respondReject(callDetails: Call.Details) {
        val isNotify = prefs.generalSettings.and(GeneralSettings.NOTIFICATION.flag) != 0
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
            if (callDetails.callerNumberVerificationStatus ==
                Connection.VERIFICATION_STATUS_PASSED) return true
        }
        return false
    }
}
