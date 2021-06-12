package me.lucky.silence

import android.content.Context

import androidx.preference.PreferenceManager

class Preferences(context: Context) {
    companion object {
        private const val SERVICE_ENABLED = "service_enabled"
        private const val CALLBACK_CHECKED = "callback_checked"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(SERVICE_ENABLED, value).apply()

    var isCallbackChecked: Boolean
        get() = prefs.getBoolean(CALLBACK_CHECKED, false)
        set(value) = prefs.edit().putBoolean(CALLBACK_CHECKED, value).apply()
}
