package me.lucky.silence

import android.content.Context

import androidx.preference.PreferenceManager

class Preferences(context: Context) {
    companion object {
        private const val SERVICE_ENABLED = "service_enabled"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(SERVICE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(SERVICE_ENABLED, value).apply()
}
