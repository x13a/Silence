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
import me.lucky.silence.Extra
import me.lucky.silence.FlagSet
import me.lucky.silence.NotificationManager
import me.lucky.silence.Preferences
import me.lucky.silence.ResponseOption
import me.lucky.silence.Sim
import me.lucky.silence.Utils
import kotlin.properties.Delegates

fun Call.Details.getRawNumber(): String? {
    return handle?.schemeSpecificPart
}

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
        isMultiSim = Utils.isSimFeatureEnabled(this)
        telephonyManager = getSystemService(TelephonyManager::class.java)
        subscriptionManager = getSystemService(SubscriptionManager::class.java)
    }

    override fun onScreenCall(callDetails: Call.Details) {
        if (!prefs.isEnabled) {
            respondAllow(callDetails)
            return
        } else if (isEmergency(callDetails)) {
            prefs.isEnabled = false
            Utils.updateMessagesEnabled(this)
            respondAllow(callDetails)
            return
        } else if (callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondAllow(callDetails)
            return
        } else if (prefs.isBlockEnabled || checkSim(prefs.simBlock)) {
            respondNotAllow(callDetails)
            return
        } else if (
            checkSim(prefs.simAllow)
            || (prefs.isRegexEnabled && checkRegex(callDetails, prefs.regexPatternAllow))
        ) {
            respondAllow(callDetails)
            return
        } else if (prefs.isRegexEnabled && checkRegex(callDetails, prefs.regexPatternBlock)) {
            respondNotAllow(callDetails)
            return
        } else if (
            (prefs.extra.has(Extra.STIR) && isStirVerified(callDetails)) ||
            (prefs.extra.has(Extra.UNKNOWN_NUMBERS) && isUnknownNumber(callDetails)) ||
            (prefs.extra.has(Extra.SHORT_NUMBERS) && isShortNumber(callDetails)) ||
            (prefs.extra.has(Extra.NOT_PLUS_NUMBERS) && isNotPlusNumber(callDetails))
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
        val disallowCall = responseOptions.has(ResponseOption.DISALLOW_CALL)
        val tel = callDetails.handle?.schemeSpecificPart
        val isNotify = !responseOptions.has(ResponseOption.SKIP_NOTIFICATION) && tel != null
        val response = CallResponse.Builder()
            .setDisallowCall(disallowCall)
            .setRejectCall(responseOptions.has(ResponseOption.REJECT_CALL) && disallowCall)
            .setSilenceCall(responseOptions.has(ResponseOption.SILENCE_CALL))
            .setSkipCallLog(responseOptions.has(ResponseOption.SKIP_CALL_LOG) && disallowCall)
            .setSkipNotification(!isNotify && disallowCall)
            .build()
        if (isNotify && disallowCall) {
            var sim: Sim? = null
            if (Utils.hasActiveMultiSim(this)) {
                sim = when {
                    checkSimSlot(0) -> Sim.SIM_1
                    checkSimSlot(1) -> Sim.SIM_2
                    else -> null
                }
            }
            notificationManager.notifyBlockedCall(tel, sim)
        }
        respondToCall(callDetails, response)
    }

    private fun isStirVerified(callDetails: Call.Details) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            callDetails.callerNumberVerificationStatus == Connection.VERIFICATION_STATUS_PASSED
        else false

    private fun isEmergency(callDetails: Call.Details): Boolean {
        val rv = callDetails.hasProperty(Call.Details.PROPERTY_EMERGENCY_CALLBACK_MODE) ||
            callDetails.hasProperty(Call.Details.PROPERTY_NETWORK_IDENTIFIED_EMERGENCY_CALL) ||
            telephonyManager
                ?.isEmergencyNumber(callDetails.getRawNumber() ?: return false) == true
        return rv
    }

    private fun checkSim(sim: FlagSet<Sim>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !isMultiSim) return false
        return (sim.has(Sim.SIM_1) && checkSimSlot(0)) ||
                (sim.has(Sim.SIM_2) && checkSimSlot(1))
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
        callDetails.getRawNumber().isNullOrBlank()

    private fun isShortNumber(callDetails: Call.Details): Boolean {
        var v = callDetails.getRawNumber() ?: return false
        if (v.startsWith('+')) v = v.drop(1)
        return v.length in 3..5 && v.isDigitsOnly()
    }

    private fun isNotPlusNumber(callDetails: Call.Details) =
        callDetails.getRawNumber()?.startsWith('+') == false

    private fun checkRegex(callDetails: Call.Details, regexPatterns: String?): Boolean {
        val phoneNumber = callDetails.getRawNumber() ?: return false
        val regexPatterns = regexPatterns?.
            split(Preferences.REGEX_SEP)?.
            map { it.trim() } ?: return false
        // Check if any of the regex patterns match the phone number
        for (pattern in regexPatterns) {
            try {
                if (pattern.toRegex(RegexOption.MULTILINE).matches(phoneNumber)) {
                    return true // Match found, allow the call
                }
            } catch (_: java.util.regex.PatternSyntaxException) {
                // Ignore invalid patterns; continue checking others
            }
        }
        // No matches found
        return false
    }
}
