package me.lucky.silence

import android.Manifest
import android.app.role.RoleManager
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

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
            Preferences.CONTACTED_CHECKED -> updateContacted()
            Preferences.REPEATED_CHECKED -> updateRepeated()
        }
    }

    private val requestCallScreeningRole =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) prefs.isServiceEnabled = true
        }

    private val requestPermissionsForContacted =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when (isGranted) {
                true -> prefs.isContactedChecked = true
                false -> binding.contactedSwitch.isChecked = false
            }
        }

    private val requestPermissionsForRepeated =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when (isGranted) {
                true -> prefs.isRepeatedChecked = true
                false -> binding.repeatedSwitch.isChecked = false
            }
        }

    private val requestPermissionsForMessage =
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
        init()
        setup()
    }

    private fun setup() {
        binding.apply {
            contactedSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (!hasReadCallLogPermission() && isChecked) {
                    true -> requestPermissionsForContacted
                        .launch(Manifest.permission.READ_CALL_LOG)
                    false -> prefs.isContactedChecked = isChecked
                }
            }
            codeSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.isCodeChecked = isChecked
            }
            codeSwitch.setOnLongClickListener {
                showCodeSettings()
                true
            }
            repeatedSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (!hasReadCallLogPermission() && isChecked) {
                    true -> requestPermissionsForRepeated
                        .launch(Manifest.permission.READ_CALL_LOG)
                    false -> prefs.isRepeatedChecked = isChecked
                }
            }
            messageSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (!hasReceiveSmsPermission() && isChecked) {
                    true -> requestPermissionsForMessage
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
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                stirSpace.visibility = View.GONE
                stirSwitch.visibility = View.GONE
                stirDescription.visibility = View.GONE
            }
            contactedSwitch.isChecked = prefs.isContactedChecked
            codeSwitch.isChecked = prefs.isCodeChecked
            repeatedSwitch.isChecked = prefs.isRepeatedChecked
            messageSwitch.isChecked = prefs.isMessageChecked
            stirSwitch.isChecked = prefs.isStirChecked
        }
    }

    private fun update() {
        updateContacted()
        updateRepeated()
        updateMessage()
        updateToggle()
        if (!Utils.hasCallScreeningRole(this) && prefs.isServiceEnabled) {
            Snackbar.make(
                findViewById(R.id.toggle),
                getString(R.string.service_unavailable_toast),
                Snackbar.LENGTH_SHORT,
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
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
                else -> repeatedSwitch.setTextColor(codeSwitch.textColors)
            }
        }
    }

    private fun updateContacted() {
        binding.apply {
            when {
                !hasReadCallLogPermission() && prefs.isContactedChecked ->
                    contactedSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> contactedSwitch.setTextColor(codeSwitch.textColors)
            }
        }
    }

    private fun updateMessage() {
        binding.apply {
            when {
                !hasReceiveSmsPermission() && prefs.isMessageChecked ->
                    messageSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> messageSwitch.setTextColor(codeSwitch.textColors)
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

    private fun showCodeSettings() {
        var codeGroups = prefs.codeGroups
        val checkedGroups = mutableListOf<Boolean>()
        for (group in CodeGroup.values()) {
            checkedGroups.add(codeGroups.and(group.flag) != 0)
        }
        MaterialAlertDialogBuilder(this)
            .setMultiChoiceItems(
                resources.getStringArray(R.array.code_groups),
                checkedGroups.toBooleanArray(),
            ) { _, index, isChecked ->
                val value = CodeGroup.values()[index]
                codeGroups = when (isChecked) {
                    true -> codeGroups.or(value.flag)
                    false -> codeGroups.and(value.flag.inv())
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setPositiveButton(R.string.save) { _, _ ->
                prefs.codeGroups = codeGroups
            }
            .show()
    }

    private fun hasReadCallLogPermission(): Boolean {
        return Utils.hasPermission(this, Manifest.permission.READ_CALL_LOG)
    }

    private fun hasReceiveSmsPermission(): Boolean {
        return Utils.hasPermission(this, Manifest.permission.RECEIVE_SMS)
    }
}
