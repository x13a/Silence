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
        private const val REPEATED_SETTINGS = "repeated_settings"
        private const val GENERAL_FLAG = "general_flag"

        // migration
        private const val CALLBACK_CHECKED = "callback_checked"
        private const val CODE_CHECKED = "code_checked"
        private const val CODE_GROUPS = "code_groups"
        private const val TOLL_FREE_CHECKED = "toll_free_checked"
        private const val MESSAGE_CHECKED = "message_checked"
        private const val SMS_CHECKED = "sms_checked"
        private const val GENERAL_SETTINGS = "general_settings"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    var isServiceEnabled: Boolean
        get() = prefs.getBoolean(SERVICE_ENABLED, false)
        set(value) = prefs.edit { putBoolean(SERVICE_ENABLED, value) }

    var isContactedChecked: Boolean
        get() = prefs.getBoolean(
            CONTACTED_CHECKED,
            prefs.getBoolean(CALLBACK_CHECKED, false),
        )
        set(value) = prefs.edit { putBoolean(CONTACTED_CHECKED, value) }

    var contacted: Int
        get() = prefs.getInt(CONTACTED, Contacted.CALL.value.or(Contacted.MESSAGE.value))
        set(value) = prefs.edit { putInt(CONTACTED, value) }

    var isGroupsChecked: Boolean
        get() = prefs.getBoolean(
            GROUPS_CHECKED,
            prefs.getBoolean(CODE_CHECKED, prefs.getBoolean(TOLL_FREE_CHECKED, false)),
        )
        set(value) = prefs.edit { putBoolean(GROUPS_CHECKED, value) }

    var groups: Int
        get() = prefs.getInt(GROUPS, prefs.getInt(
            CODE_GROUPS,
            if (prefs.getBoolean(TOLL_FREE_CHECKED, false))
                Group.TOLL_FREE.value
            else 0,
        ))
        set(value) = prefs.edit { putInt(GROUPS, value) }

    var isRepeatedChecked: Boolean
        get() = prefs.getBoolean(REPEATED_CHECKED, false)
        set(value) = prefs.edit { putBoolean(REPEATED_CHECKED, value) }

    var repeatedSettings: RepeatedSettings
        get() = RepeatedSettings.fromString(prefs.getString(
            REPEATED_SETTINGS,
            RepeatedSettings.default().toString(),
        ) ?: "")
        set(value) = prefs.edit { putString(REPEATED_SETTINGS, value.toString()) }

    var isMessagesChecked: Boolean
        get() = prefs.getBoolean(
            MESSAGES_CHECKED,
            prefs.getBoolean(MESSAGE_CHECKED, prefs.getBoolean(SMS_CHECKED, false)),
        )
        set(value) = prefs.edit { putBoolean(MESSAGES_CHECKED, value) }

    var isStirChecked: Boolean
        get() = prefs.getBoolean(STIR_CHECKED, false)
        set(value) = prefs.edit { putBoolean(STIR_CHECKED, value) }

    var generalFlag: Int
        get() = prefs.getInt(GENERAL_FLAG, prefs.getInt(GENERAL_SETTINGS, 0))
        set(value) = prefs.edit { putInt(GENERAL_FLAG, value) }

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

enum class GeneralFlag(val value: Int) {
    NOTIFICATIONS(1),
    UNKNOWN_NUMBERS(1 shl 1),
    SIM_1(1 shl 2),
    SIM_2(1 shl 3),
}

data class RepeatedSettings(val count: Int, val minutes: Int) {
    companion object {
        private const val DELIMITER = ','
        private const val DEFAULT_COUNT = 3
        private const val DEFAULT_MINUTES = 5

        fun fromString(str: String): RepeatedSettings {
            val nt = str.split(DELIMITER)
            assert(nt.size >= 2)
            return RepeatedSettings(nt[0].toInt(), nt[1].toInt())
        }

        fun default() = RepeatedSettings(DEFAULT_COUNT, DEFAULT_MINUTES)
    }

    override fun toString() = "${count}${DELIMITER}${minutes}"
}
