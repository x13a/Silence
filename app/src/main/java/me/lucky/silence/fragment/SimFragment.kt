package me.lucky.silence.fragment

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

import me.lucky.silence.Preferences
import me.lucky.silence.Sim
import me.lucky.silence.Utils
import me.lucky.silence.databinding.FragmentSimBinding

class SimFragment : Fragment() {
    private lateinit var binding: FragmentSimBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSimBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    private fun init() {
        ctx = this.requireContext()
        prefs = Preferences(ctx)
        binding.apply {
            val enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    Utils.getModemCount(ctx) >= 2
            sim1.isEnabled = enabled
            sim2.isEnabled = enabled
            val opts = prefs.sim
            sim1.isChecked = opts.and(Sim.SIM_1.value) != 0
            sim2.isChecked = opts.and(Sim.SIM_2.value) != 0
        }
    }

    private fun setup() = binding.apply {
        sim1.setOnCheckedChangeListener { _, isChecked ->
            prefs.sim = Utils.setFlag(prefs.sim, Sim.SIM_1.value, isChecked)
            if (isChecked) requestSimPermissions()
        }
        sim2.setOnCheckedChangeListener { _, isChecked ->
            prefs.sim = Utils.setFlag(prefs.sim, Sim.SIM_2.value, isChecked)
            if (isChecked) requestSimPermissions()
        }
    }

    private val registerForSimPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private fun requestSimPermissions() =
        registerForSimPermissions.launch(Manifest.permission.READ_PHONE_STATE)
}