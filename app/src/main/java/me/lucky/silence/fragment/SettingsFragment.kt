package me.lucky.silence.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import me.lucky.silence.ControlReceiver
import me.lucky.silence.Preferences
import me.lucky.silence.ResponseOption
import me.lucky.silence.Utils
import me.lucky.silence.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    private fun init() {
        ctx = this.requireContext()
        prefs = Preferences(ctx)
        binding.apply {
            controller.isChecked = Utils.isComponentEnabled(ctx, ControlReceiver::class.java)
            val opts = prefs.responseOptions
            disallowCall.isChecked = opts.and(ResponseOption.DisallowCall.value) != 0
            rejectCall.isChecked = opts.and(ResponseOption.RejectCall.value) != 0
            silenceCall.isChecked = opts.and(ResponseOption.SilenceCall.value) != 0
            skipCallLog.isChecked = opts.and(ResponseOption.SkipCallLog.value) != 0
            skipNotification.isChecked = opts.and(ResponseOption.SkipNotification.value) != 0
        }
    }

    private fun setup() = binding.apply {
        controller.setOnCheckedChangeListener { _, isChecked ->
            Utils.setComponentEnabled(ctx, ControlReceiver::class.java, isChecked)
        }
        disallowCall.setOnCheckedChangeListener { _, isChecked ->
            prefs.responseOptions = Utils.setFlag(
                prefs.responseOptions,
                ResponseOption.DisallowCall.value,
                isChecked,
            )
        }
        rejectCall.setOnCheckedChangeListener { _, isChecked ->
            prefs.responseOptions = Utils.setFlag(
                prefs.responseOptions,
                ResponseOption.RejectCall.value,
                isChecked,
            )
        }
        silenceCall.setOnCheckedChangeListener { _, isChecked ->
            prefs.responseOptions = Utils.setFlag(
                prefs.responseOptions,
                ResponseOption.SilenceCall.value,
                isChecked,
            )
        }
        skipCallLog.setOnCheckedChangeListener { _, isChecked ->
            prefs.responseOptions = Utils.setFlag(
                prefs.responseOptions,
                ResponseOption.SkipCallLog.value,
                isChecked,
            )
        }
        skipNotification.setOnCheckedChangeListener { _, isChecked ->
            prefs.responseOptions = Utils.setFlag(
                prefs.responseOptions,
                ResponseOption.SkipNotification.value,
                isChecked,
            )
        }
    }
}