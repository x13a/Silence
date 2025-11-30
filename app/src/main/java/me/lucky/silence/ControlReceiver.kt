package me.lucky.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ControlReceiver : BroadcastReceiver() {
    companion object {
        private const val PREFIX = "me.lucky.silence.action"
        private const val SET_ON = "$PREFIX.SET_ON"
        private const val SET_OFF = "$PREFIX.SET_OFF"
        private const val SET_UNKNOWN_NUMBERS_ON = "$PREFIX.SET_UNKNOWN_NUMBERS_ON"
        private const val SET_UNKNOWN_NUMBERS_OFF = "$PREFIX.SET_UNKNOWN_NUMBERS_OFF"
        private const val SET_SIM_1_ALLOW_ON = "$PREFIX.SET_SIM_1_ALLOW_ON"
        private const val SET_SIM_1_ALLOW_OFF = "$PREFIX.SET_SIM_1_ALLOW_OFF"
        private const val SET_SIM_2_ALLOW_ON = "$PREFIX.SET_SIM_2_ALLOW_ON"
        private const val SET_SIM_2_ALLOW_OFF = "$PREFIX.SET_SIM_2_ALLOW_OFF"
        private const val SET_SIM_1_BLOCK_ON = "$PREFIX.SET_SIM_1_BLOCK_ON"
        private const val SET_SIM_1_BLOCK_OFF = "$PREFIX.SET_SIM_1_BLOCK_OFF"
        private const val SET_SIM_2_BLOCK_ON = "$PREFIX.SET_SIM_2_BLOCK_ON"
        private const val SET_SIM_2_BLOCK_OFF = "$PREFIX.SET_SIM_2_BLOCK_OFF"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        when (intent?.action) {
            SET_ON -> setGlobalState(context, true)
            SET_OFF -> setGlobalState(context, false)
            SET_UNKNOWN_NUMBERS_ON -> setUnknownNumbersState(context, true)
            SET_UNKNOWN_NUMBERS_OFF -> setUnknownNumbersState(context, false)
            SET_SIM_1_ALLOW_ON -> setSimAllowState(context, Sim.SIM_1, true)
            SET_SIM_1_ALLOW_OFF -> setSimAllowState(context, Sim.SIM_1, false)
            SET_SIM_2_ALLOW_ON -> setSimAllowState(context, Sim.SIM_2, true)
            SET_SIM_2_ALLOW_OFF -> setSimAllowState(context, Sim.SIM_2, false)
            SET_SIM_1_BLOCK_ON -> setSimBlockState(context, Sim.SIM_1, true)
            SET_SIM_1_BLOCK_OFF -> setSimBlockState(context, Sim.SIM_1, false)
            SET_SIM_2_BLOCK_ON -> setSimBlockState(context, Sim.SIM_2, true)
            SET_SIM_2_BLOCK_OFF -> setSimBlockState(context, Sim.SIM_2, false)
        }
    }

    private fun setGlobalState(ctx: Context, state: Boolean) {
        Preferences(ctx).isEnabled = state
        Utils.updateMessagesEnabled(ctx)
    }

    private fun setUnknownNumbersState(ctx: Context, state: Boolean) {
        val prefs = Preferences(ctx)
        prefs.setExtra(Extra.UNKNOWN_NUMBERS, state)
    }

    private fun setSimAllowState(ctx: Context, flag: Sim, state: Boolean) {
        if (!Utils.isSimFeatureEnabled(ctx)) return
        val prefs = Preferences(ctx)
        prefs.setSimAllow(flag, state)
        if (state) prefs.setSimBlock(flag, false)
    }

    private fun setSimBlockState(ctx: Context, flag: Sim, state: Boolean) {
        if (!Utils.isSimFeatureEnabled(ctx)) return
        val prefs = Preferences(ctx)
        prefs.setSimBlock(flag, state)
        if (state) prefs.setSimAllow(flag, false)
    }
}
