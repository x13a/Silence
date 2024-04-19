package me.lucky.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ControlReceiver : BroadcastReceiver() {
    companion object {
        private const val PREFIX = "me.lucky.silence.action"
        private const val SET_ON = "$PREFIX.SET_ON"
        private const val SET_OFF = "$PREFIX.SET_OFF"
        private const val SET_UNKNOWN_NUMBERS_ON = "$PREFIX.SET_UNKNOWN_NUMBERS_ON"
        private const val SET_UNKNOWN_NUMBERS_OFF = "$PREFIX.SET_UNKNOWN_NUMBERS_OFF"
        private const val SET_SIM_1_ON = "$PREFIX.SET_SIM_1_ON"
        private const val SET_SIM_1_OFF = "$PREFIX.SET_SIM_1_OFF"
        private const val SET_SIM_2_ON = "$PREFIX.SET_SIM_2_ON"
        private const val SET_SIM_2_OFF = "$PREFIX.SET_SIM_2_OFF"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        when (intent?.action) {
            SET_ON -> setGlobalState(context, true)
            SET_OFF -> setGlobalState(context, false)
            SET_UNKNOWN_NUMBERS_ON -> setUnknownNumbersState(context, true)
            SET_UNKNOWN_NUMBERS_OFF -> setUnknownNumbersState(context, false)
            SET_SIM_1_ON -> setSimState(context, Sim.SIM_1, true)
            SET_SIM_1_OFF -> setSimState(context, Sim.SIM_1, false)
            SET_SIM_2_ON -> setSimState(context, Sim.SIM_2, true)
            SET_SIM_2_OFF -> setSimState(context, Sim.SIM_2, false)
        }
    }

    private fun setGlobalState(ctx: Context, state: Boolean) {
        Preferences(ctx).isEnabled = state
        Utils.updateMessagesTextEnabled(ctx)
    }

    private fun setUnknownNumbersState(ctx: Context, state: Boolean) {
        Preferences(ctx).isUnknownNumbersChecked = state
    }

    private fun setSimState(ctx: Context, flag: Sim, state: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            Utils.getModemCount(ctx, Modem.SUPPORTED) >= 2)
            Utils.setFlag(Preferences(ctx).sim, flag.value, state)
    }
}