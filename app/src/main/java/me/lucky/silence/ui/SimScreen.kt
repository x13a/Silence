package me.lucky.silence.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

    val sim1AllowState = remember { mutableStateOf(prefs.simAllow.has(Sim.SIM_1)) }
    val sim2AllowState = remember { mutableStateOf(prefs.simAllow.has(Sim.SIM_2)) }
    val sim1BlockState = remember { mutableStateOf(prefs.simBlock.has(Sim.SIM_1)) }
    val sim2BlockState = remember { mutableStateOf(prefs.simBlock.has(Sim.SIM_2)) }

    fun setSimAllow(
        sim: Sim,
        isChecked: Boolean,
        allowState: MutableState<Boolean>,
        blockState: MutableState<Boolean>
    ) {
        allowState.value = isChecked
        prefs.simAllow = prefs.simAllow.with(sim, isChecked)
        if (isChecked) {
            blockState.value = false
            prefs.simBlock = prefs.simBlock.with(sim, false)
            requestSimPermissions()
        }
    }

    fun setSimBlock(
        sim: Sim,
        isChecked: Boolean,
        blockState: MutableState<Boolean>,
        allowState: MutableState<Boolean>
    ) {
        blockState.value = isChecked
        prefs.simBlock = prefs.simBlock.with(sim, isChecked)
        if (isChecked) {
            allowState.value = false
            prefs.simAllow = prefs.simAllow.with(sim, false)
            requestSimPermissions()
        }
    }

    val preferenceList = listOf(
        Preference(
            getValue = { sim1AllowState.value },
            setValue = { isChecked ->
                setSimAllow(Sim.SIM_1, isChecked, sim1AllowState, sim1BlockState)
            },
            name = R.string.sim_1,
            description = R.string.sim_1_allow_description,
            state = sim1AllowState,
        ),
        Preference(
            getValue = { sim2AllowState.value },
            setValue = { isChecked ->
                setSimAllow(Sim.SIM_2, isChecked, sim2AllowState, sim2BlockState)
            },
            name = R.string.sim_2,
            description = R.string.sim_2_allow_description,
            state = sim2AllowState,
        ),
        Preference(
            getValue = { sim1BlockState.value },
            setValue = { isChecked ->
                setSimBlock(Sim.SIM_1, isChecked, sim1BlockState, sim1AllowState)
            },
            name = R.string.sim_1,
            description = R.string.sim_1_block_description,
            dividerBefore = true,
            state = sim1BlockState,
        ),
        Preference(
            getValue = { sim2BlockState.value },
            setValue = { isChecked ->
                setSimBlock(Sim.SIM_2, isChecked, sim2BlockState, sim2AllowState)
            },
            name = R.string.sim_2,
            description = R.string.sim_2_block_description,
            state = sim2BlockState,
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
