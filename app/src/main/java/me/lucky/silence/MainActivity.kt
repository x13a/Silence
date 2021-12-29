package me.lucky.silence

import android.Manifest
import android.app.role.RoleManager
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import me.lucky.silence.databinding.ActivityMainBinding

open class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val roleManager by lazy { getSystemService(RoleManager::class.java) }
    private val prefs by lazy { Preferences(this) }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            Preferences.SERVICE_ENABLED -> {
                Utils.setSmsReceiverState(this, prefs.isServiceEnabled && prefs.isSmsChecked)
                initToggle()
            }
            Preferences.SMS_CHECKED -> {
                Utils.setSmsReceiverState(this, prefs.isServiceEnabled && prefs.isSmsChecked)
                updateSms()
            }
            Preferences.CALLBACK_CHECKED -> updateCallback()
            Preferences.REPEATED_CHECKED -> updateRepeated()
        }
    }

    private val requestCallScreeningRole =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) prefs.isServiceEnabled = true
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            binding.stirSpace.visibility = View.GONE
            binding.stirSwitch.visibility = View.GONE
            binding.stirDescription.visibility = View.GONE
        }
        init()
        setup()
    }

    private fun setup() {
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
            stirSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.isStirChecked = isChecked
            }
            toggle.setOnClickListener {
                when (!Utils.hasCallScreeningRole(this@MainActivity) && !prefs.isServiceEnabled) {
                    true -> requestCallScreeningRole
                        .launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                    false -> prefs.isServiceEnabled = !prefs.isServiceEnabled
                }
            }
        }
    }

    private fun init() {
        binding.apply {
            callbackSwitch.isChecked = prefs.isCallbackChecked
            tollFreeSwitch.isChecked = prefs.isTollFreeChecked
            repeatedSwitch.isChecked = prefs.isRepeatedChecked
            smsSwitch.isChecked = prefs.isSmsChecked
            stirSwitch.isChecked = prefs.isStirChecked
        }
    }

    private fun update() {
        updateCallback()
        updateRepeated()
        updateSms()
        if (!Utils.hasCallScreeningRole(this) && prefs.isServiceEnabled) {
            Toast.makeText(
                this,
                getString(R.string.service_unavailable_toast),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
        initToggle()
        prefs.registerListener(prefsListener)
        update()
    }

    override fun onStop() {
        super.onStop()
        prefs.unregisterListener(prefsListener)
    }

    private fun updateRepeated() {
        binding.apply {
            when {
                !hasReadCallLogPermission() && prefs.isRepeatedChecked ->
                    repeatedSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> repeatedSwitch.setTextColor(tollFreeSwitch.textColors)
            }
        }
    }

    private fun updateCallback() {
        binding.apply {
            when {
                !hasReadCallLogPermission() && prefs.isCallbackChecked ->
                    callbackSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> callbackSwitch.setTextColor(tollFreeSwitch.textColors)
            }
        }
    }

    private fun updateSms() {
        binding.apply {
            when {
                !hasReceiveSmsPermission() && prefs.isSmsChecked ->
                    smsSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> smsSwitch.setTextColor(tollFreeSwitch.textColors)
            }
        }
    }

    private fun initToggle() {
        val stringId: Int
        val colorId: Int
        if (prefs.isServiceEnabled) {
            stringId = R.string.toggle_on
            colorId = R.color.icon_color_red
        } else {
            stringId = R.string.toggle_off
            colorId = R.color.icon_color_yellow
        }
        binding.toggle.apply {
            text = getText(stringId)
            setBackgroundColor(getColor(colorId))
        }
    }

    private fun hasReadCallLogPermission(): Boolean {
        return Utils.hasPermission(this, Manifest.permission.READ_CALL_LOG)
    }

    private fun hasReceiveSmsPermission(): Boolean {
        return Utils.hasPermission(this, Manifest.permission.RECEIVE_SMS)
    }
}
