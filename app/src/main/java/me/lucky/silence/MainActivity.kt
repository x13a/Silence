package me.lucky.silence

import android.Manifest
import android.app.role.RoleManager
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

import me.lucky.silence.databinding.ActivityMainBinding

open class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Preferences
    private var roleManager: RoleManager? = null

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Preferences.SERVICE_ENABLED) {
            Utils.setSmsReceiverState(
                this,
                prefs.isServiceEnabled && prefs.isMessagesChecked,
            )
            updateToggle()
        }
    }

    private val registerForCallScreeningRole =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            prefs.isServiceEnabled = it.resultCode == RESULT_OK
        }

    private val registerForContactedPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            updateContacted()
        }

    private val registerForRepeatedPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            updateRepeated()
        }

    private val registerForMessagesPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            updateMessages()
        }

    private val registerForSimPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

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
                prefs.isContactedChecked = isChecked
                if (isChecked) requestContactedPermissions() else updateContacted()
            }
            contactedSwitch.setOnLongClickListener {
                showContactedSettings()
                true
            }
            groupsSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.isGroupsChecked = isChecked
            }
            groupsSwitch.setOnLongClickListener {
                showGroupsSettings()
                true
            }
            repeatedSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.isRepeatedChecked = isChecked
                if (isChecked) requestRepeatedPermissions() else updateRepeated()
            }
            repeatedSwitch.setOnLongClickListener {
                showRepeatedSettings()
                true
            }
            messagesSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.isMessagesChecked = isChecked
                if (isChecked) requestMessagesPermissions() else updateMessages()
            }
            stirSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.isStirChecked = isChecked
            }
            toggle.setOnClickListener {
                val state = prefs.isServiceEnabled
                prefs.isServiceEnabled = !state
                if (!state) requestCallScreeningRole()
            }
            toggle.setOnLongClickListener {
                showGeneralSettings()
                true
            }
        }
    }

    private fun init() {
        prefs = Preferences(this)
        roleManager = getSystemService(RoleManager::class.java)
        NotificationManager(this).createNotificationChannels()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) hideStir()
        binding.apply {
            contactedSwitch.isChecked = prefs.isContactedChecked
            groupsSwitch.isChecked = prefs.isGroupsChecked
            repeatedSwitch.isChecked = prefs.isRepeatedChecked
            messagesSwitch.isChecked = prefs.isMessagesChecked
            stirSwitch.isChecked = prefs.isStirChecked
        }
    }

    private fun hideStir() {
        binding.apply {
            stirSpace.visibility = View.GONE
            stirSwitch.visibility = View.GONE
            stirDescription.visibility = View.GONE
        }
    }

    private fun update() {
        updateContacted()
        updateRepeated()
        updateMessages()
        updateToggle()
        if (prefs.isServiceEnabled && !Utils.hasCallScreeningRole(this))
            Snackbar.make(
                binding.toggle,
                getString(R.string.service_unavailable_popup),
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
                prefs.isRepeatedChecked && !hasRepeatedPermissions() ->
                    repeatedSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> repeatedSwitch.setTextColor(groupsSwitch.currentTextColor)
            }
        }
    }

    private fun updateContacted() {
        binding.apply {
            when {
                prefs.isContactedChecked && !hasContactedPermissions() ->
                    contactedSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> contactedSwitch.setTextColor(groupsSwitch.currentTextColor)
            }
        }
    }

    private fun updateMessages() {
        binding.apply {
            when {
                prefs.isMessagesChecked && !hasMessagesPermissions() ->
                    messagesSwitch.setTextColor(getColor(R.color.icon_color_red))
                else -> messagesSwitch.setTextColor(groupsSwitch.currentTextColor)
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
        val flags = Group.values()
        MaterialAlertDialogBuilder(this)
            .setMultiChoiceItems(
                resources.getStringArray(R.array.groups),
                flags.map { groups.and(it.value) != 0 }.toBooleanArray()
            ) { _, index, isChecked ->
                val flag = flags[index]
                groups = when (isChecked) {
                    true -> groups.or(flag.value)
                    false -> groups.and(flag.value.inv())
                }
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.groups = groups
            }
            .show()
    }

    private fun showContactedSettings() {
        var contacted = prefs.contacted
        val flags = Contacted.values()
        MaterialAlertDialogBuilder(this)
            .setMultiChoiceItems(
                resources.getStringArray(R.array.contacted),
                flags.map { contacted.and(it.value) != 0 }.toBooleanArray()
            ) { _, index, isChecked ->
                val flag = flags[index]
                contacted = when (isChecked) {
                    true -> contacted.or(flag.value)
                    false -> contacted.and(flag.value.inv())
                }
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.contacted = contacted
            }
            .show()
    }

    private fun showRepeatedSettings() {
        val itemsN = resources.getStringArray(R.array.repeated_settings_n)
        val itemsT = resources.getStringArray(R.array.repeated_settings_t)
        val repeatedSettings = prefs.repeatedSettings
        @Suppress("InflateParams")
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
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.repeatedSettings = RepeatedSettings(count, minutes)
            }
            .create()
        val updateButtonState = {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = count < minutes
        }
        n.setOnItemClickListener { _, _, position, _ ->
            count = itemsN[position].toInt()
            updateButtonState()
        }
        t.setOnItemClickListener { _, _, position, _ ->
            minutes = itemsT[position].toInt()
            updateButtonState()
        }
        dialog.show()
    }

    private fun showGeneralSettings() {
        var general = prefs.generalFlag
        val flags = GeneralFlag.values().toMutableList()
        val strings = resources.getStringArray(R.array.general_flag).toMutableList()
        val hasApi31 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val isMultiSim = Utils.getModemCount(this) >= 2
        if (!hasApi31 || !isMultiSim) {
            strings.removeAt(flags.indexOf(GeneralFlag.SIM_1))
            flags.remove(GeneralFlag.SIM_1)
            strings.removeAt(flags.indexOf(GeneralFlag.SIM_2))
            flags.remove(GeneralFlag.SIM_2)
        }
        MaterialAlertDialogBuilder(this)
            .setMultiChoiceItems(
                strings.toTypedArray(),
                flags.map { general.and(it.value) != 0 }.toBooleanArray(),
            ) { _, index, isChecked ->
                val flag = flags[index]
                general = when (isChecked) {
                    true -> general.or(flag.value)
                    false -> general.and(flag.value.inv())
                }
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.generalFlag = general
                if (hasApi31 && isMultiSim && (
                    general.and(GeneralFlag.SIM_1.value) != 0 ||
                    general.and(GeneralFlag.SIM_2.value) != 0
                )) {
                    requestSimPermissions()
                }
            }
            .show()
    }

    private fun requestContactedPermissions() {
        registerForContactedPermissions.launch(getContactedPermissions())
    }

    private fun getContactedPermissions(): Array<String> {
        val contacted = prefs.contacted
        val permissions = mutableListOf<String>()
        for (value in Contacted.values().asSequence().filter { contacted.and(it.value) != 0 }) {
            when (value) {
                Contacted.CALL -> permissions.add(Manifest.permission.READ_CALL_LOG)
                Contacted.MESSAGE -> permissions.add(Manifest.permission.READ_SMS)
            }
        }
        return permissions.toTypedArray()
    }

    private fun requestRepeatedPermissions() {
        registerForRepeatedPermissions.launch(Manifest.permission.READ_CALL_LOG)
    }

    private fun requestMessagesPermissions() {
        registerForMessagesPermissions.launch(Manifest.permission.RECEIVE_SMS)
    }

    private fun requestCallScreeningRole() {
        registerForCallScreeningRole
            .launch(roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
    }

    private fun hasContactedPermissions(): Boolean {
        return Utils.hasPermissions(this, *getContactedPermissions())
    }

    private fun hasRepeatedPermissions(): Boolean {
        return Utils.hasPermissions(this, Manifest.permission.READ_CALL_LOG)
    }

    private fun hasMessagesPermissions(): Boolean {
        return Utils.hasPermissions(this, Manifest.permission.RECEIVE_SMS)
    }

    private fun requestSimPermissions() {
        registerForSimPermissions.launch(Manifest.permission.READ_PHONE_STATE)
    }
}
