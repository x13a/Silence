package me.lucky.silence.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.ui.common.Dimension
import me.lucky.silence.ui.common.Screen

@Composable
fun RepeatedScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    var countText by rememberSaveable { mutableStateOf(prefs.repeatedCount.toString()) }
    var minutesText by rememberSaveable { mutableStateOf(prefs.repeatedMinutes.toString()) }
    var timeoutText by rememberSaveable { mutableStateOf(prefs.repeatedBurstTimeout.toString()) }

    Screen(title = R.string.repeated_main, onBackPressed = onBackPressed) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Dimension.PADDING)) {
            OutlinedTextField(
                label = { Text(stringResource(R.string.repeated_count)) },
                value = countText,
                singleLine = true,
                onValueChange = { newValue ->
                    countText = newValue.filter { it.isDigit() }
                    newValue.toIntOrNull()?.let { prefs.repeatedCount = it }
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.padding(Dimension.TEXT_FIELD_DESCRIPTION_VERTICAL_PADDING))
            OutlinedTextField(
                label = { Text(stringResource(R.string.repeated_minutes)) },
                value = minutesText,
                singleLine = true,
                onValueChange = { newValue ->
                    minutesText = newValue.filter { it.isDigit() }
                    newValue.toIntOrNull()?.let { prefs.repeatedMinutes = it }
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Text(
            stringResource(R.string.repeated_settings_description),
            fontSize = Dimension.DESCRIPTION_FONT_SIZE,
            lineHeight = Dimension.DESCRIPTION_LINE_SIZE,
            modifier = Modifier.padding(horizontal = Dimension.TEXT_FIELD_DESCRIPTION_HORIZONTAL_PADDING)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Dimension.PADDING)
        ) {
            OutlinedTextField(
                label = { Text(stringResource(R.string.repeated_burst_timeout_hint)) },
                value = timeoutText,
                singleLine = true,
                onValueChange = { newValue ->
                    timeoutText = newValue.filter { it.isDigit() }
                    newValue.toIntOrNull()?.let { prefs.repeatedBurstTimeout = it }
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Text(
            stringResource(R.string.repeated_burst_timeout_helper_text),
            fontSize = Dimension.DESCRIPTION_FONT_SIZE,
            lineHeight = Dimension.DESCRIPTION_LINE_SIZE,
            modifier = Modifier.padding(horizontal = Dimension.TEXT_FIELD_DESCRIPTION_HORIZONTAL_PADDING)
        )
    }
}

@Preview
@Composable
fun RepeatedScreenPreview() {
    RepeatedScreen(Preferences(LocalContext.current)) { true }
}