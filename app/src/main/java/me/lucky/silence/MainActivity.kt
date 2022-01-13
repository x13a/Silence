package me.lucky.silence

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

import me.lucky.silence.databinding.ActivityMainBinding

open class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var roleManager: RoleManager? = null
    private val prefs by lazy { Preferences(this) }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            Preferences.SERVICE_ENABLED -> {
                Utils.setSmsReceiverState(this, prefs.isServiceEnabled && prefs.isMessagesChecked)
                updateToggle()
            }
            Preferences.MESSAGES_CHECKED -> {
                Utils.setSmsReceiverState(this, prefs.isServiceEnabled && prefs.isMessagesChecked)
                updateMessages()
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
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            var result = true
            for (permission in arrayOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_SMS,
            )) {
                result = result && map[permission]!!
            }
            when (result) {
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

    private val requestPermissionsForMessages =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when (isGranted) {
                true -> prefs.isMessagesChecked = true
                false -> binding.messagesSwitch.isChecked = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        roleManager = getSystemService(RoleManager::class.java)
        init()
        setup()
    }

    private fun setup() {
        binding.apply {
            contactedSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (!hasContactedPermissions() && isChecked) {
                    true -> requestPermissionsForContacted
                        .launch(arrayOf(
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.READ_SMS,
                        ))
                    false -> prefs.isContactedChecked = isChecked
                }
            }
            groupsSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.isGroupsChecked = isChecked
            }
            groupsSwitch.setOnLongClickListener {
                showGroupsSettings()
                true
            }
            repeatedSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (!hasReadCallLogPermission() && isChecked) {
                    true -> requestPermissionsForRepeated
                        .launch(Manifest.permission.READ_CALL_LOG)
                    false -> prefs.isRepeatedChecked = isChecked
                }
            }
            repeatedSwitch.setOnLongClickListener {
                showRepeatedSettings()
                true
            }
            messagesSwitch.setOnCheckedChangeListener { _, isChecked ->
                when (!hasReceiveSmsPermission() && isChecked) {
                    true -> requestPermissionsForMessages
                        .launch(Manifest.permission.RECEIVE_SMS)
                    false -> prefs.isMessagesChecked = isChecked
                }
            }
            stirSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.isStirChecked = isChecked
            }
            toggle.setOnClickListener {
                when (!Utils.hasCallScreeningRole(this@MainActivity) && !prefs.isServiceEnabled) {
                    true -> requestCallScreeningRole
                        .launch(roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
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
            groupsSwitch.isChecked = prefs.isGroupsChecked
            repeatedSwitch.isChecked = prefs.isRepeatedChecked
            messagesSwitch.isChecked = prefs.isMessagesChecked
            stirSwitch.isChecked = prefs.isStirChecked
        }
    }

    private fun update() {
        updateContacted()
        updateRepeated()
        updateMessages()
        updateToggle()
        if (!Utils.hasCallScreeningRole(this) && prefs.isServiceEnabled)
            Snackbar.make(
                binding.toggle,
                getString(R.string.service_unavailable_toast),
                Snackbar.LENGTH_SHORT,
            ).show()
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
                else -> repeatedSwitch.setTextColor(groupsSwitch.textColors)
            }
        }
    }

    private fun updateContacted() {
        binding.apply {
            when {
                !hasContactedPermissions() && prefs.isContactedChecked ->
                    contactedSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> contactedSwitch.setTextColor(groupsSwitch.textColors)
            }
        }
    }

    private fun updateMessages() {
        binding.apply {
            when {
                !hasReceiveSmsPermission() && prefs.isMessagesChecked ->
                    messagesSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> messagesSwitch.setTextColor(groupsSwitch.textColors)
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

    private fun showGroupsSettings() {
        var groups = prefs.groups
        val checkedGroups = mutableListOf<Boolean>()
        for (group in Group.values()) {
            checkedGroups.add(groups.and(group.flag) != 0)
        }
        MaterialAlertDialogBuilder(this)
            .setMultiChoiceItems(
                resources.getStringArray(R.array.groups),
                checkedGroups.toBooleanArray(),
            ) { _, index, isChecked ->
                val value = Group.values()[index]
                groups = when (isChecked) {
                    true -> groups.or(value.flag)
                    false -> groups.and(value.flag.inv())
                }
            }
            .setPositiveButton(R.string.ok) { _, _ ->
                prefs.groups = groups
            }
            .show()
    }

    private fun showRepeatedSettings() {
        val itemsN = listOf("2", "3", "4", "5")
        val itemsT = listOf("3", "5", "10", "15", "20", "30", "60")
        val repeatedSettings = prefs.repeatedSettings
        @SuppressLint("InflateParams")
        val view = layoutInflater.inflate(R.layout.repeated_settings, null)
        val n = view.findViewById<AutoCompleteTextView>(R.id.repeatedSettingsCount)
        n.setText(repeatedSettings.count.toString())
        n.setAdapter(ArrayAdapter(
            view.context,
            android.R.layout.simple_spinner_dropdown_item,
            itemsN,
        ))
        val t = view.findViewById<AutoCompleteTextView>(R.id.repeatedSettingsMinutes)
        t.setText(repeatedSettings.minutes.toString())
        t.setAdapter(ArrayAdapter(
            view.context,
            android.R.layout.simple_spinner_dropdown_item,
            itemsT,
        ))
        var count = repeatedSettings.count
        var minutes = repeatedSettings.minutes
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(view)
            .setPositiveButton(R.string.ok) { _, _ ->
                prefs.repeatedSettings = RepeatedSettings(count, minutes)
            }
            .create()
        val button by lazy { dialog.getButton(AlertDialog.BUTTON_POSITIVE) }
        val updateButtonState = { button.isEnabled = count < minutes }
        n.setOnItemClickListener { _, _, position, _ ->
            count = itemsN[position].toInt()
            updateButtonState()
        }
        n.doOnTextChanged { text, _, _, _ ->
            val str = text?.toString()
            if (str == null || str == "") {
                button.isEnabled = false
                return@doOnTextChanged
            }
            count = str.toInt()
            updateButtonState()
        }
        n.setOnLongClickListener {
            n.inputType = InputType.TYPE_CLASS_NUMBER
            true
        }
        t.setOnItemClickListener { _, _, position, _ ->
            minutes = itemsT[position].toInt()
            updateButtonState()
        }
        t.doOnTextChanged { text, _, _, _ ->
            val str = text?.toString()
            if (str == null || str == "") {
                button.isEnabled = false
                return@doOnTextChanged
            }
            minutes = str.toInt()
            updateButtonState()
        }
        t.setOnLongClickListener {
            t.inputType = InputType.TYPE_CLASS_NUMBER
            true
        }
        dialog.show()
    }

    private fun hasContactedPermissions(): Boolean {
        return Utils.hasPermissions(
            this,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
        )
    }

    private fun hasReadCallLogPermission(): Boolean {
        return Utils.hasPermissions(this, Manifest.permission.READ_CALL_LOG)
    }

    private fun hasReceiveSmsPermission(): Boolean {
        return Utils.hasPermissions(this, Manifest.permission.RECEIVE_SMS)
    }
}
