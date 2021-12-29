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
        const val SMS_CHECKED = "sms_checked"
        private const val CODE_CHECKED = "code_checked"
        private const val STIR_CHECKED = "stir_checked"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    var isServiceEnabled: Boolean
        get() = getBoolean(SERVICE_ENABLED)
        set(value) = setBoolean(SERVICE_ENABLED, value)

    var isCallbackChecked: Boolean
        get() = getBoolean(CALLBACK_CHECKED)
        set(value) = setBoolean(CALLBACK_CHECKED, value)

    var isCodeChecked: Boolean
        get() = getBoolean(CODE_CHECKED)
        set(value) = setBoolean(CODE_CHECKED, value)

    var isRepeatedChecked: Boolean
        get() = getBoolean(REPEATED_CHECKED)
        set(value) = setBoolean(REPEATED_CHECKED, value)

    var isSmsChecked: Boolean
        get() = getBoolean(SMS_CHECKED)
        set(value) = setBoolean(SMS_CHECKED, value)

    var isStirChecked: Boolean
        get() = getBoolean(STIR_CHECKED)
        set(value) = setBoolean(STIR_CHECKED, value)

    private fun getBoolean(key: String): Boolean {
        return prefs.getBoolean(key, false)
    }

    private fun setBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
