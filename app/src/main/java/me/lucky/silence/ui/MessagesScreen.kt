package me.lucky.silence.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import me.lucky.silence.ui.common.Preference
import me.lucky.silence.ui.common.PreferenceList
import me.lucky.silence.ui.common.Screen
import java.util.regex.Pattern

@Composable
fun MessagesScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    val modifierDays = 'd'
    val modifierHours = 'h'
    val modifierMinutes = 'm'

    val errorHint = stringResource(R.string.messages_text_ttl_error)
    var error by remember { mutableStateOf(false) }

    val description = stringResource(R.string.messages_text_ttl_helper_text)
    var supportingText by remember { mutableStateOf(description) }

    val pattern by lazy {
        Pattern.compile("^[1-9]\\d*[${modifierDays}${modifierHours}${modifierMinutes}]$")
    }

    val ttl = prefs.messagesTextTtl
    val timeoutString = when {
        ttl % (24 * 60) == 0 -> "${ttl / 24 / 60}${modifierDays}"
        ttl % 60 == 0 -> "${ttl / 60}${modifierHours}"
        else -> "$ttl${modifierMinutes}"
    }
    var timeText by remember { mutableStateOf(timeoutString) }

    val preferenceList = listOf(
        Preference(
            getValue = { prefs.messages.and(Message.INBOX.value) != 0 },
            setValue = { isChecked ->
                prefs.messages = Utils.setFlag(prefs.messages, Message.INBOX.value, isChecked)
            },
            name = R.string.messages_inbox,
            description = R.string.messages_inbox_description,
        ), Preference(
            getValue = { prefs.messages.and(Message.TEXT.value) != 0 },
            setValue = { isChecked ->
                prefs.messages = Utils.setFlag(prefs.messages, Message.TEXT.value, isChecked)
            },
            name = R.string.messages_text,
            description = R.string.messages_text_description,
        )
    )

    Screen(title = R.string.messages_main, onBackPressed = onBackPressed) {
        PreferenceList(preferenceList)
        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            OutlinedTextField(
                label = { Text(stringResource(R.string.messages_text_ttl_hint)) },
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
                        prefs.messagesTextTtl = when (timeModifier) {
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
                    autoCorrect = false,
                    capitalization = KeyboardCapitalization.None
                )
            )
        }
    }
}

@Preview
@Composable
fun MessagesScreenPreview() {
    MessagesScreen(Preferences(LocalContext.current)) { true }
}