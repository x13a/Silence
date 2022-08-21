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
import me.lucky.silence.databinding.FragmentExtraBinding

class ExtraFragment : Fragment() {
    private lateinit var binding: FragmentExtraBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExtraBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    private fun init() {
        ctx = this.requireContext()
        prefs = Preferences(ctx)
        binding.apply {
            contacts.isChecked = prefs.isContactsChecked
            shortNumbers.isChecked = prefs.isShortNumbersChecked
            unknownNumbers.isChecked = prefs.isUnknownNumbersChecked
            plusNumbers.isChecked = prefs.isBlockPlusNumbers
            stir.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            stir.isChecked = prefs.isStirChecked
        }
    }

    private fun setup() = binding.apply {
        contacts.setOnCheckedChangeListener { _, isChecked ->
            prefs.isContactsChecked = isChecked
            if (!isChecked) requestContactsPermissions()
        }
        shortNumbers.setOnCheckedChangeListener { _, isChecked ->
            prefs.isShortNumbersChecked = isChecked
        }
        unknownNumbers.setOnCheckedChangeListener { _, isChecked ->
            prefs.isUnknownNumbersChecked = isChecked
        }
        plusNumbers.setOnCheckedChangeListener { _, isChecked ->
            prefs.isBlockPlusNumbers = isChecked
        }
        stir.setOnCheckedChangeListener { _, isChecked ->
            prefs.isStirChecked = isChecked
        }
    }

    private val registerForContactsPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private fun requestContactsPermissions() =
        registerForContactsPermissions.launch(Manifest.permission.READ_CONTACTS)
}