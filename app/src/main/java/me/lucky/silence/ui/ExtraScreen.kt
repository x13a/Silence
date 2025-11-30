package me.lucky.silence.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import me.lucky.silence.Extra
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.ui.common.Preference
import me.lucky.silence.ui.common.PreferenceList
import me.lucky.silence.ui.common.Screen

@Composable
fun ExtraScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    val registerForContactsPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun requestContactsPermissions() =
        registerForContactsPermissions.launch(Manifest.permission.READ_CONTACTS)

    val preferenceList = listOf(
        Preference(
            getValue = { prefs.extra.has(Extra.CONTACTS) },
            setValue = { isChecked ->
                prefs.setExtra(Extra.CONTACTS, isChecked)
                if (!isChecked) requestContactsPermissions()
            },
            name = R.string.extra_contacts,
            description = R.string.extra_contacts_description,
        ), Preference(
            getValue = { prefs.extra.has(Extra.SHORT_NUMBERS) },
            setValue = { isChecked -> prefs.setExtra(Extra.SHORT_NUMBERS, isChecked) },
            name = R.string.extra_short_numbers,
            description = R.string.extra_short_numbers_description,
        ), Preference(
            getValue = { prefs.extra.has(Extra.UNKNOWN_NUMBERS) },
            setValue = { isChecked -> prefs.setExtra(Extra.UNKNOWN_NUMBERS, isChecked) },
            name = R.string.extra_unknown_numbers,
            description = R.string.extra_unknown_numbers_description,
        ), Preference(
            getValue = { prefs.extra.has(Extra.NOT_PLUS_NUMBERS) },
            setValue = { isChecked -> prefs.setExtra(Extra.NOT_PLUS_NUMBERS, isChecked) },
            name = R.string.extra_plus_numbers,
            description = R.string.extra_plus_numbers_description,
        ), *(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(
                Preference(
                    getValue = { prefs.extra.has(Extra.STIR) },
                    setValue = { isChecked -> prefs.setExtra(Extra.STIR, isChecked) },
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
    })
}

@Preview
@Composable
fun ExtraScreenPreview() {
    ExtraScreen(Preferences(LocalContext.current)) { true }
}
