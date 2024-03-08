package me.lucky.silence.ui

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import me.lucky.silence.ControlReceiver
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.ResponseOption
import me.lucky.silence.Utils
import me.lucky.silence.ui.common.Preference
import me.lucky.silence.ui.common.PreferenceList
import me.lucky.silence.ui.common.Screen


@Composable
fun SettingsScreen(ctx: Context, prefs: Preferences, onBackPressed: () -> Boolean) {
    val preferenceList = listOf(
        Preference(
            getValue = { Utils.isComponentEnabled(ctx, ControlReceiver::class.java) },
            setValue = { isChecked ->
                       Utils.setComponentEnabled(ctx, ControlReceiver::class.java, isChecked)
            },
            name = R.string.settings_controller,
            description = R.string.settings_controller_description,
        ),
        Preference(
            getValue = { prefs.responseOptions.and(ResponseOption.DisallowCall.value) != 0 },
            setValue = { isChecked ->
                prefs.responseOptions = Utils.setFlag(
                    prefs.responseOptions, ResponseOption.DisallowCall.value, isChecked
                )
            },
            name = R.string.settings_disallow_call,
            description = R.string.settings_disallow_call_description,
        ),
        Preference(getValue = { prefs.responseOptions.and(ResponseOption.RejectCall.value) != 0 },
            setValue = { isChecked ->
                prefs.responseOptions =
                    Utils.setFlag(prefs.responseOptions, ResponseOption.RejectCall.value, isChecked)
            },
            name = R.string.settings_reject_call,
            description = R.string.settings_reject_call_description,
        ),
        Preference(
            getValue = { prefs.responseOptions.and(ResponseOption.SilenceCall.value) != 0 },
            setValue = { isChecked ->
                prefs.responseOptions = Utils.setFlag(
                    prefs.responseOptions, ResponseOption.SilenceCall.value, isChecked
                )
            },
            name = R.string.settings_silence_call,
            description = R.string.settings_silence_call_description,
        ),
        Preference(
            getValue = { prefs.responseOptions.and(ResponseOption.SkipCallLog.value) != 0 },
            setValue = { isChecked ->
                prefs.responseOptions = Utils.setFlag(
                    prefs.responseOptions, ResponseOption.SkipCallLog.value, isChecked
                )
            },
            name = R.string.settings_skip_call_log,
            description = R.string.settings_skip_call_log_description,
        ),
        Preference(
            getValue = { prefs.responseOptions.and(ResponseOption.SkipNotification.value) != 0 },
            setValue = { isChecked ->
                prefs.responseOptions = Utils.setFlag(
                    prefs.responseOptions, ResponseOption.SkipNotification.value, isChecked
                )
            },
            name = R.string.settings_skip_notification,
            description = R.string.settings_skip_notification_description,
        )
    )
    Screen(title = R.string.settings,
        onBackPressed = onBackPressed,
        content = { PreferenceList(preferenceList) })
}

@Preview
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(LocalContext.current, Preferences(LocalContext.current)) { true }
    }
}