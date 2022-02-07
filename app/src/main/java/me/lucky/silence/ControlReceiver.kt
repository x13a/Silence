package me.lucky.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ControlReceiver : BroadcastReceiver() {
    companion object {
        private const val SET_ON = "me.lucky.silence.action.SET_ON"
        private const val SET_OFF = "me.lucky.silence.action.SET_OFF"
        private const val SET_HIDDEN_NUMBERS_ON = "me.lucky.silence.action.SET_HIDDEN_NUMBERS_ON"
        private const val SET_HIDDEN_NUMBERS_OFF = "me.lucky.silence.action.SET_HIDDEN_NUMBERS_OFF"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        when (intent.action) {
            SET_ON -> setGlobalState(context, true)
            SET_OFF -> setGlobalState(context, false)
            SET_HIDDEN_NUMBERS_ON -> setHiddenNumbersState(context, true)
            SET_HIDDEN_NUMBERS_OFF -> setHiddenNumbersState(context, false)
        }
    }

    private fun setGlobalState(ctx: Context, state: Boolean) {
        Preferences(ctx).apply {
            isServiceEnabled = state
            Utils.setSmsReceiverState(ctx, isServiceEnabled && isMessagesChecked)
        }
    }

    private fun setHiddenNumbersState(ctx: Context, state: Boolean) {
        Preferences(ctx).apply {
            generalFlag = when (state) {
                true -> generalFlag.or(GeneralFlag.HIDDEN_NUMBERS.value)
                false -> generalFlag.and(GeneralFlag.HIDDEN_NUMBERS.value.inv())
            }
        }
    }
}
