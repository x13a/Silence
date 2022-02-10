package me.lucky.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ControlReceiver : BroadcastReceiver() {
    companion object {
        private const val SET_ON = "me.lucky.silence.action.SET_ON"
        private const val SET_OFF = "me.lucky.silence.action.SET_OFF"
        private const val SET_UNKNOWN_NUMBERS_ON =
            "me.lucky.silence.action.SET_UNKNOWN_NUMBERS_ON"
        private const val SET_UNKNOWN_NUMBERS_OFF =
            "me.lucky.silence.action.SET_UNKNOWN_NUMBERS_OFF"
        private const val SET_SIM_1_ON = "me.lucky.silence.action.SET_SIM_1_ON"
        private const val SET_SIM_1_OFF = "me.lucky.silence.action.SET_SIM_1_OFF"
        private const val SET_SIM_2_ON = "me.lucky.silence.action.SET_SIM_2_ON"
        private const val SET_SIM_2_OFF = "me.lucky.silence.action.SET_SIM_2_OFF"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        when (intent.action) {
            SET_ON -> setGlobalState(context, true)
            SET_OFF -> setGlobalState(context, false)
            SET_UNKNOWN_NUMBERS_ON ->
                setGeneralFlag(context, GeneralFlag.UNKNOWN_NUMBERS, true)
            SET_UNKNOWN_NUMBERS_OFF ->
                setGeneralFlag(context, GeneralFlag.UNKNOWN_NUMBERS, false)
            SET_SIM_1_ON -> setSimState(context, GeneralFlag.SIM_1, true)
            SET_SIM_1_OFF -> setSimState(context, GeneralFlag.SIM_1, false)
            SET_SIM_2_ON -> setSimState(context, GeneralFlag.SIM_2, true)
            SET_SIM_2_OFF -> setSimState(context, GeneralFlag.SIM_2, false)
        }
    }

    private fun setGlobalState(ctx: Context, state: Boolean) {
        Preferences(ctx).apply {
            isServiceEnabled = state
            Utils.setSmsReceiverState(ctx, state && isMessagesChecked)
        }
    }

    private fun setGeneralFlag(ctx: Context, flag: GeneralFlag, state: Boolean) {
        Preferences(ctx).apply {
            generalFlag = when (state) {
                true -> generalFlag.or(flag.value)
                false -> generalFlag.and(flag.value.inv())
            }
        }
    }

    private fun setSimState(ctx: Context, flag: GeneralFlag, state: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Utils.getModemCount(ctx) >= 2)
            setGeneralFlag(ctx, flag, state)
    }
}
