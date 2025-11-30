package me.lucky.silence.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import me.lucky.silence.Contact
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.ui.common.Preference
import me.lucky.silence.ui.common.PreferenceList
import me.lucky.silence.ui.common.Screen


@Composable
fun ContactedScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    fun getContactedPermissions(): Array<String> {
        val permissions = mutableSetOf<String>()
        for (value in prefs.contacted.active()) {
            when (value) {
                Contact.CALL_OUT -> permissions.add(Manifest.permission.READ_CALL_LOG)
                Contact.MESSAGE_OUT -> permissions.add(Manifest.permission.READ_SMS)
                Contact.CALL_IN -> permissions.add(Manifest.permission.READ_CALL_LOG)
                Contact.MESSAGE_IN -> permissions.add(Manifest.permission.READ_SMS)
            }
        }
        return permissions.toTypedArray()
    }

    val registerForContactedPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    fun requestContactedPermissions() =
        registerForContactedPermissions.launch(getContactedPermissions())

    val preferenceList = listOf(
        Preference(
            getValue = { prefs.contacted.has(Contact.CALL_OUT) },
            setValue = { isChecked ->
                prefs.setContacted(Contact.CALL_OUT, isChecked)
                if (isChecked) requestContactedPermissions()
            },
            name = R.string.contacted_call_out,
            description = R.string.contacted_call_out_description,
        ), Preference(
            getValue = { prefs.contacted.has(Contact.MESSAGE_OUT) },
            setValue = { isChecked ->
                prefs.setContacted(Contact.MESSAGE_OUT, isChecked)
                if (isChecked) requestContactedPermissions()
            },
            name = R.string.contacted_message_out,
            description = R.string.contacted_message_out_description,
        ), Preference(
            getValue = { prefs.contacted.has(Contact.CALL_IN) },
            setValue = { isChecked ->
                prefs.setContacted(Contact.CALL_IN, isChecked)
                if (isChecked) requestContactedPermissions()
            },
            name = R.string.contacted_call_in,
            description = R.string.contacted_call_in_description,
        ),
        Preference(
            getValue = { prefs.contacted.has(Contact.MESSAGE_IN) },
            setValue = { isChecked ->
                prefs.setContacted(Contact.MESSAGE_IN, isChecked)
                if (isChecked) requestContactedPermissions()
            },
            name = R.string.contacted_message_in,
            description = R.string.contacted_message_in_description,
        ),
    )

    Screen(
        title = R.string.contacted_main,
        onBackPressed = onBackPressed,
        content = { PreferenceList(preferenceList) }
    )
}

@Preview
@Composable
fun ContactedScreenPreview() {
    ContactedScreen(Preferences(LocalContext.current)) { true }
}
