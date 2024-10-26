package me.lucky.silence.ui

import android.app.AlertDialog
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
    var showSmsDialog by remember { mutableStateOf(false) }
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
        ),
        Preference(
            getValue = { prefs.smsMessage!!.isNotEmpty() },
            setValue = { isChecked ->
                if (isChecked) {
                    showSmsDialog = true
                } else {
                    prefs.smsMessage = ""
                }
            },
            name = R.string.enter_sms_message,
            description = R.string.enter_sms_message_description,
        )
    )

    Screen(title = R.string.settings,
        onBackPressed = onBackPressed,
        content = { PreferenceList(preferenceList) })
    if (showSmsDialog) {
        SmsMessageDialog(
            onDismiss = { showSmsDialog = false },
            onConfirm = { message ->
                prefs.smsMessage = message
                showSmsDialog = false
            }
        )
    }
}


@Preview
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen(LocalContext.current, Preferences(LocalContext.current)) { true }
    }

}

@Composable
fun SmsMessageDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val context = LocalContext.current
    val input = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = stringResource(id = R.string.enter_sms_message))
        },
        text = {
            Column {
                Text(text = stringResource(id = R.string.enter_sms_message_description))
                TextField(
                    value = input.value,
                    onValueChange = { input.value = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(input.value)
            }) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        }
    )
}
