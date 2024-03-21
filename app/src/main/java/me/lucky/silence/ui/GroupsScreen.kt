package me.lucky.silence.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import me.lucky.silence.Group
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.Utils
import me.lucky.silence.ui.common.Preference
import me.lucky.silence.ui.common.PreferenceList
import me.lucky.silence.ui.common.Screen


@Composable
fun GroupsScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    val preferenceList = listOf(
        Preference(
            getValue = { prefs.groups.and(Group.LOCAL.value) != 0 },
            setValue = { isChecked ->
                prefs.groups = Utils.setFlag(prefs.groups, Group.LOCAL.value, isChecked)
            },
            name = R.string.groups_local,
            description = R.string.groups_local_description,
        ), Preference(
            getValue = { prefs.groups.and(Group.NOT_LOCAL.value) != 0 },
            setValue = { isChecked ->
                prefs.groups = Utils.setFlag(prefs.groups, Group.NOT_LOCAL.value, isChecked)
            },
            name = R.string.groups_not_local,
            description = R.string.groups_not_local_description,
        ), Preference(
            getValue = { prefs.groups.and(Group.TOLL_FREE.value) != 0 },
            setValue = { isChecked ->
                prefs.groups = Utils.setFlag(prefs.groups, Group.TOLL_FREE.value, isChecked)
            },
            name = R.string.groups_toll_free,
            description = R.string.groups_toll_free_description,
        ), Preference(
            getValue = { prefs.groups.and(Group.MOBILE.value) != 0 },
            setValue = { isChecked ->
                prefs.groups = Utils.setFlag(prefs.groups, Group.MOBILE.value, isChecked)
            },
            name = R.string.groups_mobile,
            description = R.string.groups_mobile_description,
        ), Preference(
            getValue = { prefs.groups.and(Group.LOCAL_MOBILE.value) != 0 },
            setValue = { isChecked ->
                prefs.groups = Utils.setFlag(prefs.groups, Group.LOCAL_MOBILE.value, isChecked)
            },
            name = R.string.groups_local_mobile,
            description = R.string.groups_local_mobile_description,
        )
    )

    Screen(title = R.string.groups_main,
        onBackPressed = onBackPressed,
        content = { PreferenceList(preferenceList) })
}

@Preview
@Composable
fun GroupsScreenPreview() {
    GroupsScreen(Preferences(LocalContext.current)) { true }
}