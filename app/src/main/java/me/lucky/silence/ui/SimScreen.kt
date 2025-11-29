package me.lucky.silence.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.Sim
import me.lucky.silence.ui.common.Preference
import me.lucky.silence.ui.common.PreferenceList
import me.lucky.silence.ui.common.Screen


@Composable
fun SimScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    val registerForSimPermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    fun requestSimPermissions() =
        registerForSimPermissions.launch(Manifest.permission.READ_PHONE_STATE)

    val preferenceList = listOf(
        Preference(
            getValue = { prefs.simAllow.has(Sim.SIM_1) },
            setValue = { isChecked ->
                prefs.simAllow = prefs.simAllow.with(Sim.SIM_1, isChecked)
                if (isChecked) requestSimPermissions()
            },
            name = R.string.sim_1,
            description = R.string.sim_1_allow_description,
        ),
        Preference(
            getValue = { prefs.simAllow.has(Sim.SIM_2) },
            setValue = { isChecked ->
                prefs.simAllow = prefs.simAllow.with(Sim.SIM_2, isChecked)
                if (isChecked) requestSimPermissions()
            },
            name = R.string.sim_2,
            description = R.string.sim_2_allow_description
        ),
        Preference(
            getValue = { prefs.simBlock.has(Sim.SIM_1) },
            setValue = { isChecked ->
                prefs.simBlock = prefs.simBlock.with(Sim.SIM_1, isChecked)
                if (isChecked) requestSimPermissions()
            },
            name = R.string.sim_1,
            description = R.string.sim_1_block_description,
            dividerBefore = true,
        ),
        Preference(
            getValue = { prefs.simBlock.has(Sim.SIM_2) },
            setValue = { isChecked ->
                prefs.simBlock = prefs.simBlock.with(Sim.SIM_2, isChecked)
                if (isChecked) requestSimPermissions()
            },
            name = R.string.sim_2,
            description = R.string.sim_2_block_description
        )
    )
    Screen(title = R.string.sim,
        onBackPressed = onBackPressed,
        content = { PreferenceList(preferenceList) })
}

@Preview
@Composable
fun SimScreenPreview() {
    SimScreen(Preferences(LocalContext.current)) { true }
}
