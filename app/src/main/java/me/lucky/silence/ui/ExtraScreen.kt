package me.lucky.silence.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import me.lucky.silence.Extra
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.ui.common.Preference
import me.lucky.silence.ui.common.PreferenceList
import me.lucky.silence.ui.common.Screen

@Composable
fun ExtraScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    val ctx = LocalContext.current
    val contactsEnabledState = remember { mutableStateOf(prefs.extra.has(Extra.CONTACTS)) }
    
    LaunchedEffect(Unit) {
        if (contactsEnabledState.value && ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            // Si estaba activado pero ya no tiene permisos, desactivarlo
            contactsEnabledState.value = false
            prefs.setExtra(Extra.CONTACTS, false)
        }
    }
    
    val registerForContactsPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            contactsEnabledState.value = isGranted
            prefs.setExtra(Extra.CONTACTS, isGranted)
            if (!isGranted) {
                Toast.makeText(
                    ctx,
                    "Contacts permission not granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    val preferenceList = listOf(
        Preference(
            getValue = { contactsEnabledState.value },
            setValue = { isChecked ->
                if (isChecked) {
                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        contactsEnabledState.value = true
                        prefs.setExtra(Extra.CONTACTS, true)
                    } else {
                        registerForContactsPermissions.launch(Manifest.permission.READ_CONTACTS)
                    }
                } else {
                    contactsEnabledState.value = false
                    prefs.setExtra(Extra.CONTACTS, false)
                }
            },
            name = R.string.extra_contacts,
            description = R.string.extra_contacts_description,
            state = contactsEnabledState,
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
