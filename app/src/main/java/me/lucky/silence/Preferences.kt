package me.lucky.silence

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class Preferences(ctx: Context) {
    companion object {
        const val ENABLED = "enabled"
        private const val CONTACTED_CHECKED = "contacted_checked"
        private const val REPEATED_CHECKED = "repeated_checked"
        private const val MESSAGES_CHECKED = "messages_checked"
        private const val GROUPS_CHECKED = "groups_checked"
        private const val STIR_CHECKED = "stir_checked"

        private const val CONTACTED = "contacted"
        private const val GROUPS = "groups"
        private const val REPEATED_COUNT = "repeated_count"
        private const val REPEATED_MINUTES = "repeated_minutes"
        private const val MESSAGES = "messages"
        private const val MESSAGES_TEXT_TTL = "messages_text_ttl"

        private const val RESPONSE_OPTIONS = "call_screening_response_options"
        private const val UNKNOWN_NUMBERS_CHECKED = "unknown_numbers_checked"
        private const val SHORT_NUMBERS_CHECKED = "short_numbers_checked"
        private const val SIM = "sim"

        private const val DEFAULT_REPEATED_COUNT = 3
        private const val DEFAULT_REPEATED_MINUTES = 5
        private const val DEFAULT_MESSAGES_TEXT_TTL = 2 * 24 * 60

        // migration
        private const val SERVICE_ENABLED = "service_enabled"
        private const val GENERAL_UNKNOWN_NUMBERS_CHECKED = "general_unknown_numbers_checked"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    var isEnabled: Boolean
        get() = prefs.getBoolean(ENABLED, prefs.getBoolean(SERVICE_ENABLED, false))
        set(value) = prefs.edit { putBoolean(ENABLED, value) }

    var isContactedChecked: Boolean
        get() = prefs.getBoolean(CONTACTED_CHECKED, false)
        set(value) = prefs.edit { putBoolean(CONTACTED_CHECKED, value) }

    var contacted: Int
        get() = prefs.getInt(CONTACTED, Contact.CALL.value.or(Contact.MESSAGE.value))
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

    var messages: Int
        get() = prefs.getInt(MESSAGES, 0)
        set(value) = prefs.edit { putInt(MESSAGES, value) }

    var messagesTextTtl: Int
        get() = prefs.getInt(MESSAGES_TEXT_TTL, DEFAULT_MESSAGES_TEXT_TTL)
        set(value) = prefs.edit { putInt(MESSAGES_TEXT_TTL, value) }

    var isStirChecked: Boolean
        get() = prefs.getBoolean(STIR_CHECKED, false)
        set(value) = prefs.edit { putBoolean(STIR_CHECKED, value) }

    var isUnknownNumbersChecked: Boolean
        get() = prefs.getBoolean(
            UNKNOWN_NUMBERS_CHECKED,
            prefs.getBoolean(GENERAL_UNKNOWN_NUMBERS_CHECKED, false),
        )
        set(value) = prefs.edit { putBoolean(UNKNOWN_NUMBERS_CHECKED, value) }

    var isShortNumbersChecked: Boolean
        get() = prefs.getBoolean(SHORT_NUMBERS_CHECKED, false)
        set(value) = prefs.edit { putBoolean(SHORT_NUMBERS_CHECKED, value) }

    var responseOptions: Int
        get() = prefs.getInt(
            RESPONSE_OPTIONS,
            ResponseOption.DisallowCall.value.or(ResponseOption.RejectCall.value),
        )
        set(value) = prefs.edit { putInt(RESPONSE_OPTIONS, value) }

    var sim: Int
        get() = prefs.getInt(SIM, 0)
        set(value) = prefs.edit { putInt(SIM, value) }
}

enum class Contact(val value: Int) {
    CALL(1),
    MESSAGE(1 shl 1),
}

enum class Group(val value: Int) {
    TOLL_FREE(1),
    LOCAL(1 shl 1),
    NOT_LOCAL(1 shl 2),
    MOBILE(1 shl 3),
}

enum class Message(val value: Int) {
    INBOX(1),
    TEXT(1 shl 1),
}

enum class ResponseOption(val value: Int) {
    DisallowCall(1),
    RejectCall(1 shl 1),
    SilenceCall(1 shl 2),
    SkipCallLog(1 shl 3),
    SkipNotification(1 shl 4),
}

enum class Sim(val value: Int) {
    SIM_1(1),
    SIM_2(1 shl 1),
}