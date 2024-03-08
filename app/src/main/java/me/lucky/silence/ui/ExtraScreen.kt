package me.lucky.silence.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.ui.common.Preference
import me.lucky.silence.ui.common.PreferenceList
import me.lucky.silence.ui.common.Screen


@Composable
fun ExtraScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    val regexErrorHint = stringResource(R.string.regex_pattern_error)
    var regexError by remember { mutableStateOf(false) }
    val regexDescription = stringResource(R.string.regex_pattern_helper_text)
    var regexSupportingText by remember { mutableStateOf(regexDescription) }
    var regexText by remember { mutableStateOf(prefs.regexPattern ?: "") }

    val registerForContactsPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun requestContactsPermissions() =
        registerForContactsPermissions.launch(Manifest.permission.READ_CONTACTS)

    val preferenceList = listOf(
        Preference(
            getValue = { prefs.isContactsChecked },
            setValue = { isChecked ->
                prefs.isContactsChecked = isChecked
                if (!isChecked) requestContactsPermissions()
            },
            name = R.string.extra_contacts,
            description = R.string.extra_contacts_description,
        ), Preference(
            getValue = { prefs.isShortNumbersChecked },
            setValue = { isChecked -> prefs.isShortNumbersChecked = isChecked },
            name = R.string.extra_short_numbers,
            description = R.string.extra_short_numbers_description,
        ), Preference(
            getValue = { prefs.isUnknownNumbersChecked },
            setValue = { isChecked -> prefs.isUnknownNumbersChecked = isChecked },
            name = R.string.extra_unknown_numbers,
            description = R.string.extra_unknown_numbers_description,
        ), Preference(
            getValue = { prefs.isBlockPlusNumbers },
            setValue = { isChecked -> prefs.isBlockPlusNumbers = isChecked },
            name = R.string.extra_plus_numbers,
            description = R.string.extra_plus_numbers_description,
        ), *(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(
                Preference(
                    getValue = { prefs.isStirChecked },
                    setValue = { isChecked -> prefs.isStirChecked = isChecked },
                    name = R.string.extra_stir,
                    description = R.string.extra_stir_description,
                )
            )
        } else {
            emptyArray()
        })
    )

    Screen(title = R.string.extra, onBackPressed = onBackPressed, content = {
        PreferenceList(preferenceList)
        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            OutlinedTextField(
                label = { Text(stringResource(R.string.regex_main)) },
                supportingText = { Text(regexSupportingText) },
                singleLine = true,
                value = regexText,
                isError = regexError,
                onValueChange = { newValue ->
                    regexError = !isValidRegex(newValue)
                    if (regexError) {
                        regexSupportingText = regexErrorHint
                    } else {
                        regexSupportingText = regexDescription
                        prefs.regexPattern = newValue
                    }
                    regexText = newValue
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    autoCorrect = false,
                    capitalization = KeyboardCapitalization.None
                )
            )
        }
    })
}

private fun isValidRegex(pattern: String): Boolean {
    try {
        pattern.toRegex(RegexOption.MULTILINE)
    } catch (exc: java.util.regex.PatternSyntaxException) {
        return false
    }
    return true
}

@Preview
@Composable
fun ExtraScreenPreview() {
    ExtraScreen(Preferences(LocalContext.current)) { true }
}