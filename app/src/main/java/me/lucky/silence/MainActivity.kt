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
                Utils.setSmsReceiverState(this, prefs.isServiceEnabled && prefs.isMessageChecked)
                updateToggle()
            }
            Preferences.MESSAGE_CHECKED -> {
                Utils.setSmsReceiverState(this, prefs.isServiceEnabled && prefs.isMessageChecked)
                updateMessage()
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
                true -> prefs.isMessageChecked = true
                false -> binding.messageSwitch.isChecked = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            binding.apply {
                stirSpace.visibility = View.GONE
                stirSwitch.visibility = View.GONE
                stirDescription.visibility = View.GONE
            }
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
            prefixSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.isPrefixChecked = isChecked
            }
            repeatedSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (!hasReadCallLogPermission() && isChecked) {
                    true -> requestReadCallLogPermissionForRepeated
                        .launch(Manifest.permission.READ_CALL_LOG)
                    false -> prefs.isRepeatedChecked = isChecked
                }
            }
            messageSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (!hasReceiveSmsPermission() && isChecked) {
                    true -> requestReceiveSmsPermission
                        .launch(Manifest.permission.RECEIVE_SMS)
                    false -> prefs.isMessageChecked = isChecked
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
            prefixSwitch.isChecked = prefs.isPrefixChecked
            repeatedSwitch.isChecked = prefs.isRepeatedChecked
            messageSwitch.isChecked = prefs.isMessageChecked
            stirSwitch.isChecked = prefs.isStirChecked
        }
    }

    private fun update() {
        updateCallback()
        updateRepeated()
        updateMessage()
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
        updateToggle()
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
                else -> repeatedSwitch.setTextColor(prefixSwitch.textColors)
            }
        }
    }

    private fun updateCallback() {
        binding.apply {
            when {
                !hasReadCallLogPermission() && prefs.isCallbackChecked ->
                    callbackSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> callbackSwitch.setTextColor(prefixSwitch.textColors)
            }
        }
    }

    private fun updateMessage() {
        binding.apply {
            when {
                !hasReceiveSmsPermission() && prefs.isMessageChecked ->
                    messageSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> messageSwitch.setTextColor(prefixSwitch.textColors)
            }
        }
    }

    private fun updateToggle() {
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
