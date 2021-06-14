package me.lucky.silence

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import me.lucky.silence.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val roleManager by lazy { getSystemService(RoleManager::class.java) }
    private val prefs by lazy { Preferences(this) }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Preferences.SERVICE_ENABLED) {
            updateToggleUi(prefs.isServiceEnabled)
        }
    }

    private val requestCallScreeningRole =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            toggle(true)
        }
    }
    private val requestCallLogPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> prefs.isCallbackChecked = true
                else -> binding.callbackSwitch.isChecked = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs.registerListener(prefsListener)
        setupUiListeners()
    }

    private fun setupUiListeners() {
        binding.callbackSwitch.setOnCheckedChangeListener { _, isChecked ->
            when {
                !hasCallLogPermission() && isChecked -> requestCallLogPermission
                    .launch(Manifest.permission.READ_CALL_LOG)
                else -> prefs.isCallbackChecked = isChecked
            }
        }

        binding.tollFreeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.isTollFreeChecked = isChecked
        }

        binding.repeatedSwitch.setOnCheckedChangeListener { _, isChecked ->
            when {
                !hasCallLogPermission() && isChecked -> requestCallLogPermission
                    .launch(Manifest.permission.READ_CALL_LOG)
                else -> prefs.isRepeatedChecked = isChecked
            }
        }

        binding.toggle.setOnClickListener {
            val isNotEnabled = !prefs.isServiceEnabled
            when {
                !hasCallScreeningRole() && isNotEnabled -> requestCallScreeningRole
                    .launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                else -> toggle(isNotEnabled)
            }
        }
    }

    private fun updateStates() {
        val hasCallLogPerm = hasCallLogPermission()
        binding.callbackSwitch.isChecked = when {
            !hasCallLogPerm -> {
                prefs.isCallbackChecked = false
                false
            }
            else -> prefs.isCallbackChecked
        }

        binding.tollFreeSwitch.isChecked = prefs.isTollFreeChecked
        binding.repeatedSwitch.isChecked = when {
            !hasCallLogPerm -> {
                prefs.isRepeatedChecked = false
                false
            }
            else -> prefs.isRepeatedChecked
        }
        when {
            !hasCallScreeningRole() -> prefs.isServiceEnabled = false
            else -> updateToggleUi(prefs.isServiceEnabled)
        }
    }

    override fun onStart() {
        super.onStart()
        updateStates()
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterListener(prefsListener)
    }

    private fun toggle(isChecked: Boolean) {
        prefs.isServiceEnabled = isChecked
    }

    private fun updateToggleUi(isChecked: Boolean) {
        val stringId: Int
        val colorId: Int
        if (isChecked) {
            stringId = R.string.toggle_on
            colorId = R.color.red
        } else {
            stringId = R.string.toggle_off
            colorId = R.color.green
        }
        binding.toggle.apply {
            text = getText(stringId)
            backgroundTintList = getColorStateList(colorId)
        }
    }

    private fun hasCallScreeningRole(): Boolean {
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun hasCallLogPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED)
    }
}
