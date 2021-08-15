package me.lucky.silence

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import me.lucky.silence.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val roleManager by lazy { getSystemService(RoleManager::class.java) }
    private val prefs by lazy { Preferences(this) }
    private val db by lazy { AppDatabase.getInstance(this).smsFilterDao() }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            Preferences.SERVICE_ENABLED -> {
                setSmsReceiverState(prefs.isServiceEnabled && prefs.isSmsChecked)
                updateToggle()
            }
            Preferences.SMS_CHECKED -> {
                if (!prefs.isSmsChecked) db.deleteInactive()
                setSmsReceiverState(prefs.isServiceEnabled && prefs.isSmsChecked)
                updateSms()
            }
        }
    }

    private val requestCallScreeningRole =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) prefs.isServiceEnabled = true
        }

    private val requestReadCallLogPermissionForCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when (isGranted) {
                true -> prefs.isCallbackChecked = true
                false -> binding.callbackSwitch.isChecked = false
            }
        }

    private val requestReadCallLogPermissionForRepeated =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when (isGranted) {
                true -> prefs.isRepeatedChecked = true
                false -> binding.repeatedSwitch.isChecked = false
            }
        }

    private val requestReceiveSmsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when (isGranted) {
                true -> prefs.isSmsChecked = true
                false -> binding.smsSwitch.isChecked = false
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
        binding.apply {
            callbackSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (!hasReadCallLogPermission() && isChecked) {
                    true -> requestReadCallLogPermissionForCallback
                        .launch(Manifest.permission.READ_CALL_LOG)
                    false -> prefs.isCallbackChecked = isChecked
                }
            }

            tollFreeSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.isTollFreeChecked = isChecked
            }

            repeatedSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (!hasReadCallLogPermission() && isChecked) {
                    true -> requestReadCallLogPermissionForRepeated
                        .launch(Manifest.permission.READ_CALL_LOG)
                    false -> prefs.isRepeatedChecked = isChecked
                }
            }

            smsSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (!hasReceiveSmsPermission() && isChecked) {
                    true -> requestReceiveSmsPermission
                        .launch(Manifest.permission.RECEIVE_SMS)
                    false -> prefs.isSmsChecked = isChecked
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                stirSwitch.setOnCheckedChangeListener { _, isChecked ->
                    prefs.isStirChecked = isChecked
                }
            }

            toggle.setOnClickListener {
                when (!hasCallScreeningRole() && !prefs.isServiceEnabled) {
                    true -> requestCallScreeningRole
                        .launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                    false -> prefs.isServiceEnabled = !prefs.isServiceEnabled
                }
            }
        }
    }

    private fun updateStates() {
        updateCallback()
        updateTollFree()
        updateRepeated()
        updateSms()
        updateStir()
        updateToggle()

        if (!hasCallScreeningRole() && prefs.isServiceEnabled) {
            Toast.makeText(
                this,
                getString(R.string.service_unavailable_toast),
                Toast.LENGTH_SHORT,
            ).show()
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

    private fun updateRepeated() {
        if (!hasReadCallLogPermission()) prefs.isRepeatedChecked = false
        binding.repeatedSwitch.isChecked = prefs.isRepeatedChecked
    }

    private fun updateCallback() {
        if (!hasReadCallLogPermission()) prefs.isCallbackChecked = false
        binding.callbackSwitch.isChecked = prefs.isCallbackChecked
    }

    private fun updateTollFree() {
        binding.tollFreeSwitch.isChecked = prefs.isTollFreeChecked
    }

    private fun updateSms() {
        if (!hasReceiveSmsPermission()) prefs.isSmsChecked = false
        binding.smsSwitch.isChecked = prefs.isSmsChecked
    }

    private fun updateStir() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.stirSwitch.isChecked = prefs.isStirChecked
        }
    }

    private fun updateToggle() {
        val stringId: Int
        val colorId: Int
        if (prefs.isServiceEnabled) {
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

    private fun hasReadCallLogPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun hasReceiveSmsPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun setSmsReceiverState(value: Boolean) {
        packageManager.setComponentEnabledSetting(
            ComponentName(this, SmsReceiver::class.java),
            if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
    }
}
