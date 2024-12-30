package me.lucky.silence.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.ui.common.Screen


@Composable
fun RegexScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    val regexErrorHint = stringResource(R.string.regex_pattern_error)
    var regexAllowError by remember { mutableStateOf(false) }
    var regexBlockError by remember { mutableStateOf(false) }
    val regexAllowedDescription = stringResource(R.string.regex_pattern_allow_helper_text)
    var regexAllowedSupportingText by remember { mutableStateOf(regexAllowedDescription) }
    val regexBlockedDescription = stringResource(R.string.regex_pattern_block_helper_text)
    var regexBlockedSupportingText by remember { mutableStateOf(regexBlockedDescription) }
    var regexAllowText by remember { mutableStateOf(prefs.regexPatternAllow ?: "") }
    var regexBlockText by remember { mutableStateOf(prefs.regexPatternBlock ?: "") }

    Screen(title = R.string.regex_main, onBackPressed = onBackPressed, content = {
        Column  {
            Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                OutlinedTextField(
                    label = { Text(stringResource(R.string.regex_pattern_allow_hint)) },
                    supportingText = { Text(regexAllowedSupportingText) },
                    singleLine = false,
                    value = regexAllowText,
                    isError = regexAllowError,
                    onValueChange = { newValue ->
                        regexAllowError = !isValidRegex(newValue)
                        if (regexAllowError) {
                            regexAllowedSupportingText = regexErrorHint
                        } else {
                            regexAllowedSupportingText = regexAllowedDescription
                            prefs.regexPatternAllow = newValue
                        }
                        regexAllowText = newValue
                    },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        autoCorrect = false,
                        capitalization = KeyboardCapitalization.None
                    )
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                OutlinedTextField(
                    label = { Text(stringResource(R.string.regex_pattern_block_hint)) },
                    supportingText = { Text(regexBlockedSupportingText) },
                    singleLine = false,
                    value = regexBlockText,
                    isError = regexBlockError,
                    onValueChange = { newValue ->
                        regexBlockError = !isValidRegex(newValue)
                        if (regexBlockError) {
                            regexBlockedSupportingText = regexErrorHint
                        } else {
                            regexBlockedSupportingText = regexBlockedDescription
                            prefs.regexPatternBlock = newValue
                        }
                        regexBlockText = newValue
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
    })
}

private fun isValidRegex(patterns: String): Boolean {
    // Split the input string by commas and trim whitespace from each pattern
    val regexPatterns = patterns.split(",").map { it.trim() }

    // Validate each pattern
    for (pattern in regexPatterns) {
        try {
            // Attempt to convert the pattern to a Regex object
            pattern.toRegex(RegexOption.MULTILINE)
        } catch (exc: java.util.regex.PatternSyntaxException) {
            // If an exception is thrown, return false
            return false
        }
    }

    // All patterns are valid
    return true
}

@Preview
@Composable
fun RegexScreenPreview() {
    RegexScreen(Preferences(LocalContext.current)) { true }
}