package me.lucky.silence

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class Preferences(ctx: Context) {
    companion object {
        const val SERVICE_ENABLED = "service_enabled"
        const val CALLBACK_CHECKED = "callback_checked"
        const val REPEATED_CHECKED = "repeated_checked"
        const val MESSAGE_CHECKED = "message_checked"
        private const val CODE_CHECKED = "code_checked"
        private const val STIR_CHECKED = "stir_checked"

        // migrate
        private const val TOLL_FREE_CHECKED = "toll_free_checked"
        private const val SMS_CHECKED = "sms_checked"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(SERVICE_ENABLED, false)
        set(value) = prefs.edit { putBoolean(SERVICE_ENABLED, value) }

    var isCallbackChecked: Boolean
        get() = prefs.getBoolean(CALLBACK_CHECKED, false)
        set(value) = prefs.edit { putBoolean(CALLBACK_CHECKED, value) }

    var isCodeChecked: Boolean
        get() = prefs.getBoolean(CODE_CHECKED, prefs.getBoolean(TOLL_FREE_CHECKED, false))
        set(value) = prefs.edit { putBoolean(CODE_CHECKED, value) }

    var isRepeatedChecked: Boolean
        get() = prefs.getBoolean(REPEATED_CHECKED, false)
        set(value) = prefs.edit { putBoolean(REPEATED_CHECKED, value) }

    var isMessageChecked: Boolean
        get() = prefs.getBoolean(MESSAGE_CHECKED, prefs.getBoolean(SMS_CHECKED, false))
        set(value) = prefs.edit { putBoolean(MESSAGE_CHECKED, value) }

    var isStirChecked: Boolean
        get() = prefs.getBoolean(STIR_CHECKED, false)
        set(value) = prefs.edit { putBoolean(STIR_CHECKED, value) }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
