package me.lucky.silence.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import me.lucky.silence.Contact
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.Utils
import me.lucky.silence.ui.common.Preference
import me.lucky.silence.ui.common.PreferenceList
import me.lucky.silence.ui.common.Screen


@Composable
fun ContactedScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    val preferenceList = listOf(
        Preference(
            getValue = { prefs.contacted.and(Contact.CALL.value) != 0 },
            setValue = { isChecked ->
                prefs.contacted = Utils.setFlag(prefs.contacted, Contact.CALL.value, isChecked)
            },
            name = R.string.contacted_call,
            description = R.string.contacted_call_description,
        ), Preference(
            getValue = { prefs.contacted.and(Contact.MESSAGE.value) != 0 },
            setValue = { isChecked ->
                prefs.contacted = Utils.setFlag(prefs.contacted, Contact.MESSAGE.value, isChecked)
            },
            name = R.string.contacted_message,
            description = R.string.contacted_message_description,
        ), Preference(
            getValue = { prefs.contacted.and(Contact.ANSWER.value) != 0 },
            setValue = { isChecked ->
                prefs.contacted = Utils.setFlag(prefs.contacted, Contact.ANSWER.value, isChecked)
            },
            name = R.string.contacted_answer,
            description = R.string.contacted_answer_description,
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