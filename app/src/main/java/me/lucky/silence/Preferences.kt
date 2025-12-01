package me.lucky.silence

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class Preferences(ctx: Context) {
    companion object {
        const val ENABLED = "enabled"
        const val REPEATED_ENABLED = "repeated_checked"
        const val REGEX_ENABLED = "regex_checked"
        const val BLOCK_ENABLED = "block_enabled"

        const val CONTACTED = "contacted"
        const val GROUPS = "groups"
        const val EXTRA = "extra"
        const val REPEATED_COUNT = "repeated_count"
        const val REPEATED_MINUTES = "repeated_minutes"
        const val REPEATED_BURST_TIMEOUT = "repeated_burst_timeout"
        const val MESSAGES = "messages"
        const val MESSAGES_TTL = "messages_text_ttl"

        const val RESPONSE_OPTIONS = "call_screening_response_options"
        const val SIM_ALLOW = "sim_allow"
        const val SIM_BLOCK = "sim_block"

        const val REGEX_PATTERN_ALLOW = "regex_pattern_allow"
        const val REGEX_PATTERN_BLOCK = "regex_pattern_block"
        const val REGEX_SEP = ";"

        const val DEFAULT_REPEATED_COUNT = 3
        const val DEFAULT_REPEATED_MINUTES = 5
        const val DEFAULT_MESSAGES_TTL = 2 * 24 * 60

        // migration
        const val NOT_PLUS_NUMBERS_CHECKED = "not_plus_numbers_checked"
        const val UNKNOWN_NUMBERS_CHECKED = "unknown_numbers_checked"
        const val SHORT_NUMBERS_CHECKED = "short_numbers_checked"
        const val CONTACTS_CHECKED = "contacts_checked"
        const val STIR_CHECKED = "stir_checked"
        const val REGEX_PATTERN = "regex_pattern"
        const val SIM = "sim"
        const val GENERAL_UNKNOWN_NUMBERS_CHECKED = "general_unknown_numbers_checked"

        const val MIGRATION_VERSION = "migration_version"
        const val CURRENT_MIGRATION_VERSION = 1
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    var isEnabled: Boolean
        get() = prefs.getBoolean(ENABLED, false)
        set(value) = prefs.edit { putBoolean(ENABLED, value) }

    var contacted: FlagSet<Contact>
        get() = FlagSet.from(
            prefs.getInt(
                CONTACTED,
                Contact.CALL_OUT.value.or(Contact.MESSAGE_OUT.value),
            ),
        )
        set(flag) = prefs.edit { putInt(CONTACTED, flag.value) }

    var groups: FlagSet<Group>
        get() = FlagSet.from(prefs.getInt(GROUPS, 0))
        set(flag) = prefs.edit { putInt(GROUPS, flag.value) }

    var isRepeatedEnabled: Boolean
        get() = prefs.getBoolean(REPEATED_ENABLED, false)
        set(value) = prefs.edit { putBoolean(REPEATED_ENABLED, value) }

    var repeatedCount: Int
        get() = prefs.getInt(REPEATED_COUNT, DEFAULT_REPEATED_COUNT)
        set(value) = prefs.edit { putInt(REPEATED_COUNT, value) }

    var repeatedMinutes: Int
        get() = prefs.getInt(REPEATED_MINUTES, DEFAULT_REPEATED_MINUTES)
        set(value) = prefs.edit { putInt(REPEATED_MINUTES, value) }

    var repeatedBurstTimeout: Int
        get() = prefs.getInt(REPEATED_BURST_TIMEOUT, 0)
        set(value) = prefs.edit { putInt(REPEATED_BURST_TIMEOUT, value) }

    var messages: FlagSet<Message>
        get() = FlagSet.from(prefs.getInt(MESSAGES, 0))
        set(flag) = prefs.edit { putInt(MESSAGES, flag.value) }

    var messagesTtl: Int
        get() = prefs.getInt(MESSAGES_TTL, DEFAULT_MESSAGES_TTL)
        set(value) = prefs.edit { putInt(MESSAGES_TTL, value) }

    var responseOptions: FlagSet<ResponseOption>
        get() = FlagSet.from(
            prefs.getInt(
                RESPONSE_OPTIONS,
                ResponseOption.DISALLOW_CALL.value.or(ResponseOption.REJECT_CALL.value),
            ),
        )
        set(flag) = prefs.edit { putInt(RESPONSE_OPTIONS, flag.value) }

    var simAllow: FlagSet<Sim>
        get() = FlagSet.from(prefs.getInt(SIM_ALLOW, prefs.getInt(SIM, 0)))
        set(flag) = prefs.edit { putInt(SIM_ALLOW, flag.value) }

    var simBlock: FlagSet<Sim>
        get() = FlagSet.from(prefs.getInt(SIM_BLOCK, 0))
        set(flag) = prefs.edit { putInt(SIM_BLOCK, flag.value) }

    var isBlockEnabled: Boolean
        get() = prefs.getBoolean(BLOCK_ENABLED, false)
        set(value) = prefs.edit { putBoolean(BLOCK_ENABLED, value) }

    fun setContacted(flag: Contact, enabled: Boolean) {
        contacted = contacted.with(flag, enabled)
    }

    fun setGroups(flag: Group, enabled: Boolean) {
        groups = groups.with(flag, enabled)
    }

    fun setMessages(flag: Message, enabled: Boolean) {
        messages = messages.with(flag, enabled)
    }

    fun setResponseOption(flag: ResponseOption, enabled: Boolean) {
        responseOptions = responseOptions.with(flag, enabled)
    }

    fun setSimAllow(flag: Sim, enabled: Boolean) {
        simAllow = simAllow.with(flag, enabled)
    }

    fun setSimBlock(flag: Sim, enabled: Boolean) {
        simBlock = simBlock.with(flag, enabled)
    }

    var extra: FlagSet<Extra>
        get() = FlagSet.from(
            if (prefs.contains(EXTRA)) prefs.getInt(EXTRA, 0) else defaultExtra(),
        )
        set(flag) = prefs.edit { putInt(EXTRA, flag.value) }

    fun setExtra(flag: Extra, enabled: Boolean) {
        extra = extra.with(flag, enabled)
    }

    fun resetToDefaults() {
        prefs.edit {
            putBoolean(ENABLED, false)
            putBoolean(BLOCK_ENABLED, false)
        }
    }

    fun runMigrationIfNeeded() {
        val currentVersion = prefs.getInt(MIGRATION_VERSION, 0)
        if (currentVersion < CURRENT_MIGRATION_VERSION) {
            // Migration to fix default values
            resetToDefaults()
            prefs.edit { putInt(MIGRATION_VERSION, CURRENT_MIGRATION_VERSION) }
        }
    }

    private fun defaultExtra(): Int {
        var flags = 0
        if (prefs.getBoolean(CONTACTS_CHECKED, true))
            flags = flags.or(Extra.CONTACTS.value)
        if (prefs.getBoolean(SHORT_NUMBERS_CHECKED, false))
            flags = flags.or(Extra.SHORT_NUMBERS.value)
        if (prefs.getBoolean(
                UNKNOWN_NUMBERS_CHECKED,
                prefs.getBoolean(GENERAL_UNKNOWN_NUMBERS_CHECKED, false),
            )
        ) flags = flags.or(Extra.UNKNOWN_NUMBERS.value)
        if (prefs.getBoolean(NOT_PLUS_NUMBERS_CHECKED, false))
            flags = flags.or(Extra.NOT_PLUS_NUMBERS.value)
        if (prefs.getBoolean(STIR_CHECKED, false))
            flags = flags.or(Extra.STIR.value)
        return flags
    }
    var isRegexEnabled: Boolean
        get() = prefs.getBoolean(REGEX_ENABLED, false)
        set(value) = prefs.edit { putBoolean(REGEX_ENABLED, value) }

    var regexPatternAllow: String?
        get() = prefs.getString(
            REGEX_PATTERN_ALLOW,
            prefs.getString(REGEX_PATTERN, "")
        )
        set(value) = prefs.edit { putString(REGEX_PATTERN_ALLOW, value) }

    var regexPatternBlock: String?
        get() = prefs.getString(REGEX_PATTERN_BLOCK, "")
        set(value) = prefs.edit { putString(REGEX_PATTERN_BLOCK, value) }
}

enum class Contact(override val value: Int) : FlagValue {
    CALL_OUT(1),
    MESSAGE_OUT(1 shl 1),
    CALL_IN(1 shl 2),
    MESSAGE_IN(1 shl 3),
}

enum class Group(override val value: Int) : FlagValue {
    TOLL_FREE(1),
    LOCAL(1 shl 1),
    NOT_LOCAL(1 shl 2),
    MOBILE(1 shl 3),
    LOCAL_MOBILE(1 shl 4),
}

enum class Message(override val value: Int) : FlagValue {
    SMS(1),
    NOTIFICATION(1 shl 1),
}

enum class Extra(override val value: Int) : FlagValue {
    CONTACTS(1),
    SHORT_NUMBERS(1 shl 1),
    UNKNOWN_NUMBERS(1 shl 2),
    NOT_PLUS_NUMBERS(1 shl 3),
    STIR(1 shl 4),
}

enum class ResponseOption(override val value: Int) : FlagValue {
    DISALLOW_CALL(1),
    REJECT_CALL(1 shl 1),
    SILENCE_CALL(1 shl 2),
    SKIP_CALL_LOG(1 shl 3),
    SKIP_NOTIFICATION(1 shl 4),
}

enum class Sim(override val value: Int) : FlagValue {
    SIM_1(1),
    SIM_2(1 shl 1),
}
