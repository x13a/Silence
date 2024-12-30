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
import me.lucky.silence.Modem
import me.lucky.silence.NotificationManager
import me.lucky.silence.Preferences
import me.lucky.silence.ResponseOption
import me.lucky.silence.Sim
import me.lucky.silence.Utils
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
        isMultiSim = Utils.getModemCount(this, Modem.SUPPORTED) >= 2
        telephonyManager = getSystemService(TelephonyManager::class.java)
        subscriptionManager = getSystemService(SubscriptionManager::class.java)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        if (!prefs.isEnabled) {
            respondAllow(callDetails)
            return
        } else if (isEmergency(callDetails)) {
            prefs.isEnabled = false
            Utils.setMessagesTextEnabled(this, false)
            respondAllow(callDetails)
            return
        } else if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondAllow(callDetails)
            return
        } else if (checkAllowRegex(callDetails)) {
            respondAllow(callDetails)
            return
        } else if (
            prefs.isBlockEnabled ||
            checkBlockRegex(callDetails)
        ) {
            respondNotAllow(callDetails)
            return
        } else if (
            (prefs.isStirChecked && isStirVerified(callDetails)) ||
            (prefs.isUnknownNumbersChecked && isUnknownNumber(callDetails)) ||
            (prefs.isShortNumbersChecked && isShortNumber(callDetails)) ||
            (prefs.isNotPlusNumbersChecked && isNotPlusNumber(callDetails)) ||
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
        } catch (_: NumberParseException) {
            respondNotAllow(callDetails)
            return
        }
        if (callScreeningHelper.check(number, callDetails)) respondAllow(callDetails)
        else respondNotAllow(callDetails)
    }

    private fun respondAllow(callDetails: Call.Details) =
        respondToCall(callDetails, CallResponse.Builder().build())

    private fun respondNotAllow(callDetails: Call.Details) {
        val responseOptions = prefs.responseOptions
        val disallowCall = responseOptions.and(ResponseOption.DisallowCall.value) != 0
        val tel = callDetails.handle?.schemeSpecificPart
        val isNotify = responseOptions.and(ResponseOption.SkipNotification.value) == 0
                && tel != null
        val response = CallResponse.Builder()
            .setDisallowCall(disallowCall)
            .setRejectCall(responseOptions.and(ResponseOption.RejectCall.value) != 0
                    && disallowCall)
            .setSilenceCall(responseOptions.and(ResponseOption.SilenceCall.value) != 0)
            .setSkipCallLog(responseOptions.and(ResponseOption.SkipCallLog.value) != 0
                    && disallowCall)
            .setSkipNotification(!isNotify && disallowCall)
            .build()
        if (isNotify && disallowCall) {
            var sim: Sim? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                Utils.getModemCount(this, Modem.ACTIVE) >= 2) {
                sim = when {
                    checkSimSlot(0) -> Sim.SIM_1
                    checkSimSlot(1) -> Sim.SIM_2
                    else -> null
                }
            }
            notificationManager.notifyBlockedCall(tel ?: return, sim)
        }
        respondToCall(callDetails, response)
    }

    private fun isStirVerified(callDetails: Call.Details) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            callDetails.callerNumberVerificationStatus == Connection.VERIFICATION_STATUS_PASSED
        else false

    private fun isEmergency(callDetails: Call.Details) =
        callDetails.hasProperty(Call.Details.PROPERTY_EMERGENCY_CALLBACK_MODE) ||
        callDetails.hasProperty(Call.Details.PROPERTY_NETWORK_IDENTIFIED_EMERGENCY_CALL) ||
        telephonyManager
            ?.isEmergencyNumber(callDetails.handle?.schemeSpecificPart ?: "") == true

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
        } catch (_: SecurityException) { false }
    }

    private fun isUnknownNumber(callDetails: Call.Details) =
        callDetails.handle?.schemeSpecificPart == null

    private fun isShortNumber(callDetails: Call.Details): Boolean {
        var v = callDetails.handle?.schemeSpecificPart ?: return false
        if (v.startsWith('+')) v = v.drop(1)
        return v.length in 3..5 && v.isDigitsOnly()
    }

    private fun isNotPlusNumber(callDetails: Call.Details) =
        callDetails.handle?.schemeSpecificPart?.startsWith('+') == false

    private fun checkAllowRegex(callDetails: Call.Details): Boolean {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: return false
        val regexPatterns = prefs.regexPatternAllow?.split(",")?.map { it.trim() } ?: return false

        // Check if any of the regex patterns match the phone number
        for (pattern in regexPatterns) {
            try {
                if (pattern.toRegex(RegexOption.MULTILINE).matches(phoneNumber)) {
                    return true // Match found, allow the call
                }
            } catch (exc: java.util.regex.PatternSyntaxException) {
                // Ignore invalid patterns; continue checking others
            }
        }

        // No matches found
        return false
    }

    private fun checkBlockRegex(callDetails: Call.Details): Boolean {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: return false
        val regexPatterns = prefs.regexPatternBlock?.split(",")?.map { it.trim() } ?: return false

        // Check if any of the regex patterns match the phone number
        for (pattern in regexPatterns) {
            try {
                if (pattern.toRegex(RegexOption.MULTILINE).matches(phoneNumber)) {
                    return true // Match found, block the call
                }
            } catch (exc: java.util.regex.PatternSyntaxException) {
                // Ignore invalid patterns; continue checking others
            }
        }

        // No matches found
        return false
    }
}