package me.lucky.silence

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class Preferences(ctx: Context) {
    companion object {
        const val SERVICE_ENABLED = "service_enabled"
        private const val CONTACTED_CHECKED = "contacted_checked"
        private const val REPEATED_CHECKED = "repeated_checked"
        private const val MESSAGES_CHECKED = "messages_checked"
        private const val GROUPS_CHECKED = "groups_checked"
        private const val STIR_CHECKED = "stir_checked"

        private const val CONTACTED = "contacted"
        private const val GROUPS = "groups"
        private const val REPEATED_COUNT = "repeated_count"
        private const val REPEATED_MINUTES = "repeated_minutes"
        private const val GENERAL_NOTIFICATIONS_CHECKED = "general_notifications_checked"
        private const val GENERAL_UNKNOWN_NUMBERS_CHECKED = "general_unknown_numbers_checked"
        private const val SIM = "sim"

        private const val DEFAULT_REPEATED_COUNT = 3
        private const val DEFAULT_REPEATED_MINUTES = 5
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(SERVICE_ENABLED, false)
        set(value) = prefs.edit { putBoolean(SERVICE_ENABLED, value) }

    var isContactedChecked: Boolean
        get() = prefs.getBoolean(CONTACTED_CHECKED, false)
        set(value) = prefs.edit { putBoolean(CONTACTED_CHECKED, value) }

    var contacted: Int
        get() = prefs.getInt(CONTACTED, Contacted.CALL.value.or(Contacted.MESSAGE.value))
        set(value) = prefs.edit { putInt(CONTACTED, value) }

    var isGroupsChecked: Boolean
        get() = prefs.getBoolean(GROUPS_CHECKED, false)
        set(value) = prefs.edit { putBoolean(GROUPS_CHECKED, value) }

    var groups: Int
        get() = prefs.getInt(GROUPS, 0)
        set(value) = prefs.edit { putInt(GROUPS, value) }

    var isRepeatedChecked: Boolean
        get() = prefs.getBoolean(REPEATED_CHECKED, false)
        set(value) = prefs.edit { putBoolean(REPEATED_CHECKED, value) }

    var repeatedCount: Int
        get() = prefs.getInt(REPEATED_COUNT, DEFAULT_REPEATED_COUNT)
        set(value) = prefs.edit { putInt(REPEATED_COUNT, value) }

    var repeatedMinutes: Int
        get() = prefs.getInt(REPEATED_MINUTES, DEFAULT_REPEATED_MINUTES)
        set(value) = prefs.edit { putInt(REPEATED_MINUTES, value) }

    var isMessagesChecked: Boolean
        get() = prefs.getBoolean(MESSAGES_CHECKED, false)
        set(value) = prefs.edit { putBoolean(MESSAGES_CHECKED, value) }

    var isStirChecked: Boolean
        get() = prefs.getBoolean(STIR_CHECKED, false)
        set(value) = prefs.edit { putBoolean(STIR_CHECKED, value) }

    var isGeneralNotificationsChecked: Boolean
        get() = prefs.getBoolean(GENERAL_NOTIFICATIONS_CHECKED, false)
        set(value) = prefs.edit { putBoolean(GENERAL_NOTIFICATIONS_CHECKED, value) }

    var isGeneralUnknownNumbersChecked: Boolean
        get() = prefs.getBoolean(GENERAL_UNKNOWN_NUMBERS_CHECKED, false)
        set(value) = prefs.edit { putBoolean(GENERAL_UNKNOWN_NUMBERS_CHECKED, value) }

    var sim: Int
        get() = prefs.getInt(SIM, 0)
        set(value) = prefs.edit { putInt(SIM, value) }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}

enum class Contacted(val value: Int) {
    CALL(1),
    MESSAGE(1 shl 1),
}

enum class Group(val value: Int) {
    TOLL_FREE(1),
    LOCAL(1 shl 1),
}

enum class Sim(val value: Int) {
    SIM_1(1),
    SIM_2(1 shl 1),
}
