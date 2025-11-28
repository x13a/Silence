package me.lucky.silence.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.lucky.silence.Message
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.Utils
import me.lucky.silence.ui.common.Dimension
import me.lucky.silence.ui.common.Preference
import me.lucky.silence.ui.common.PreferenceList
import me.lucky.silence.ui.common.Screen
import java.util.regex.Pattern

@Composable
fun MessagesScreen(ctx: Context, prefs: Preferences, onBackPressed: () -> Boolean) {
    val modifierDays = 'd'
    val modifierHours = 'h'
    val modifierMinutes = 'm'

    val errorHint = stringResource(R.string.messages_ttl_error)
    var error by remember { mutableStateOf(false) }

    val description = stringResource(R.string.messages_ttl_helper_text)
    var supportingText by remember { mutableStateOf(description) }

    val pattern by lazy {
        Pattern.compile("^[1-9]\\d*[${modifierDays}${modifierHours}${modifierMinutes}]$")
    }

    val ttl = prefs.messagesTtl
    val timeoutString = when {
        ttl % (24 * 60) == 0 -> "${ttl / 24 / 60}${modifierDays}"
        ttl % 60 == 0 -> "${ttl / 60}${modifierHours}"
        else -> "$ttl${modifierMinutes}"
    }
    var timeText by remember { mutableStateOf(timeoutString) }
    val registerForMessagePermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun requestMessagePermissions() =
        registerForMessagePermissions.launch(Manifest.permission.READ_SMS)

    val preferenceList = listOf(
        Preference(
            getValue = { prefs.messages.and(Message.MESSAGE.value) != 0 },
            setValue = { isChecked ->
                prefs.messages = Utils.setFlag(
                    prefs.messages,
                    Message.MESSAGE.value,
                    isChecked
                )
                if (isChecked) requestMessagePermissions()
                Utils.updateMessagesEnabled(ctx)
            },
            name = R.string.messages_message,
            description = R.string.messages_message_description,
        ),
        Preference(
            getValue = { prefs.messages.and(Message.NOTIFICATION.value) != 0 },
            setValue = { isChecked ->
                prefs.messages = Utils.setFlag(
                    prefs.messages,
                    Message.NOTIFICATION.value,
                    isChecked
                )
                Utils.updateMessagesEnabled(ctx)
            },
            name = R.string.messages_notification,
            description = R.string.messages_notification_description,
        )
    )

    Screen(title = R.string.messages_main, onBackPressed = onBackPressed) {
        PreferenceList(preferenceList)
        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            OutlinedTextField(
                label = { Text(stringResource(R.string.messages_ttl_hint)) },
                supportingText = { Text(supportingText) },
                singleLine = true,
                value = timeText,
                isError = error,
                onValueChange = { newValue ->
                    if (!pattern.matcher(newValue).matches()) {
                        error = true
                        supportingText = errorHint
                    } else {
                        error = false
                        supportingText = description
                        val timeModifier = newValue.last()
                        val i = newValue.dropLast(1).toInt()
                        prefs.messagesTtl = when (timeModifier) {
                            modifierDays -> i * 24 * 60
                            modifierHours -> i * 60
                            else -> i
                        }
                    }
                    timeText = newValue
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    autoCorrectEnabled = false,
                    capitalization = KeyboardCapitalization.None
                )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimension.PADDING),
            onClick = {
                ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
        ) {
            Text(
                text = stringResource(R.string.goto_button),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Preview
@Composable
fun MessagesScreenPreview() {
    MessagesScreen(LocalContext.current, Preferences(LocalContext.current)) { true }
}