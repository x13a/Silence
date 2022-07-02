package me.lucky.silence.fragments

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import me.lucky.silence.*
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || Utils.getModemCount(ctx) < 2) hideSim()
        binding.apply {
            controller.isChecked = Utils.getComponentState(ctx, ControlReceiver::class.java)
            unknownNumbers.isChecked = prefs.isUnknownNumbersChecked
            val opts = prefs.responseOptions
            disallowCall.isChecked = opts.and(ResponseOption.DisallowCall.value) != 0
            rejectCall.isChecked = opts.and(ResponseOption.RejectCall.value) != 0
            silenceCall.isChecked = opts.and(ResponseOption.SilenceCall.value) != 0
            skipCallLog.isChecked = opts.and(ResponseOption.SkipCallLog.value) != 0
            skipNotification.isChecked = opts.and(ResponseOption.SkipNotification.value) != 0
            val simOpts = prefs.sim
            sim1.isChecked = simOpts.and(Sim.SIM_1.value) != 0
            sim2.isChecked = simOpts.and(Sim.SIM_2.value) != 0
        }
    }

    private fun hideSim() {
        binding.apply {
            space1.visibility = View.GONE
            space2.visibility = View.GONE
            divider1.visibility = View.GONE
            sim1.visibility = View.GONE
            sim2.visibility = View.GONE
            sim1Description.visibility = View.GONE
            sim2Description.visibility = View.GONE
        }
    }

    private fun setup() {
        binding.apply {
            controller.setOnCheckedChangeListener { _, isChecked ->
                Utils.setComponentState(ctx, ControlReceiver::class.java, isChecked)
            }
            unknownNumbers.setOnCheckedChangeListener { _, isChecked ->
                prefs.isUnknownNumbersChecked = isChecked
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
            sim1.setOnCheckedChangeListener { _, isChecked ->
                prefs.sim = Utils.setFlag(prefs.sim, Sim.SIM_1.value, isChecked)
                if (isChecked) requestSimPermissions()
            }
            sim2.setOnCheckedChangeListener { _, isChecked ->
                prefs.sim = Utils.setFlag(prefs.sim, Sim.SIM_2.value, isChecked)
                if (isChecked) requestSimPermissions()
            }
        }
    }

    private val registerForSimPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private fun requestSimPermissions() =
        registerForSimPermissions.launch(Manifest.permission.READ_PHONE_STATE)
}