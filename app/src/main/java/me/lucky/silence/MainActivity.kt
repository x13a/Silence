package me.lucky.silence

import android.Manifest
import android.app.role.RoleManager
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
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
                prefs.isServiceEnabled &&
                        prefs.isMessagesChecked &&
                        prefs.messages.and(Message.BODY.value) != 0,
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
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
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
            messagesSwitch.setOnLongClickListener {
                showMessagesSettings()
                true
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
            .setTitle(R.string.groups_switch)
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

    private fun showMessagesSettings() {
        var messages = prefs.messages
        val flags = Message.values()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.messages_switch)
            .setMultiChoiceItems(
                resources.getStringArray(R.array.messages),
                flags.map { messages.and(it.value) != 0 }.toBooleanArray()
            ) { _, index, isChecked ->
                val flag = flags[index]
                messages = when (isChecked) {
                    true -> messages.or(flag.value)
                    false -> messages.and(flag.value.inv())
                }
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.messages = messages
            }
            .show()
    }

    private fun showContactedSettings() {
        var contacted = prefs.contacted
        val flags = Contact.values()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.contacted_switch)
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
        val itemsN = resources.getStringArray(R.array.repeated_count)
        val itemsT = resources.getStringArray(R.array.repeated_minutes)
        var count = prefs.repeatedCount
        var minutes = prefs.repeatedMinutes
        @Suppress("InflateParams")
        val view = layoutInflater.inflate(R.layout.repeated_settings, null)
        val n = view.findViewById<AutoCompleteTextView>(R.id.repeatedCount)
        n.setText(count.toString())
        n.setAdapter(ArrayAdapter(
            view.context,
            android.R.layout.simple_spinner_dropdown_item,
            itemsN,
        ))
        val t = view.findViewById<AutoCompleteTextView>(R.id.repeatedMinutes)
        t.setText(minutes.toString())
        t.setAdapter(ArrayAdapter(
            view.context,
            android.R.layout.simple_spinner_dropdown_item,
            itemsT,
        ))
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.repeated_switch)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.repeatedCount = count
                prefs.repeatedMinutes = minutes
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
        var isNotificationsChecked = prefs.isGeneralNotificationsChecked
        var isUnknownNumbersChecked = prefs.isGeneralUnknownNumbersChecked
        var sim = prefs.sim
        @Suppress("InflateParams")
        val view = layoutInflater.inflate(R.layout.general_settings, null)
        val hasApi31 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val isMultiSim = Utils.getModemCount(this) >= 2
        val notifications = view.findViewById<CheckBox>(R.id.notifications)
        val unknownNumbers = view.findViewById<CheckBox>(R.id.unknownNumbers)
        val sim1 = view.findViewById<CheckBox>(R.id.sim1)
        val sim2 = view.findViewById<CheckBox>(R.id.sim2)
        notifications.isChecked = isNotificationsChecked
        unknownNumbers.isChecked = isUnknownNumbersChecked
        sim1.isChecked = sim.and(Sim.SIM_1.value) != 0
        sim2.isChecked = sim.and(Sim.SIM_2.value) != 0
        if (!hasApi31 || !isMultiSim) {
            view.findViewById<View>(R.id.divider).visibility = View.GONE
            sim1.visibility = View.GONE
            sim2.visibility = View.GONE
        }
        notifications.setOnCheckedChangeListener { _, isChecked ->
            isNotificationsChecked = isChecked
        }
        unknownNumbers.setOnCheckedChangeListener { _, isChecked ->
            isUnknownNumbersChecked = isChecked
        }
        sim1.setOnCheckedChangeListener { _, isChecked ->
            sim = when (isChecked) {
                true -> sim.or(Sim.SIM_1.value)
                false -> sim.and(Sim.SIM_1.value.inv())
            }
        }
        sim2.setOnCheckedChangeListener { _, isChecked ->
            sim = when (isChecked) {
                true -> sim.or(Sim.SIM_2.value)
                false -> sim.and(Sim.SIM_2.value.inv())
            }
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                prefs.isGeneralNotificationsChecked = isNotificationsChecked
                prefs.isGeneralUnknownNumbersChecked = isUnknownNumbersChecked
                prefs.sim = sim
                if (hasApi31 && isMultiSim && (
                    sim.and(Sim.SIM_1.value) != 0 ||
                    sim.and(Sim.SIM_2.value) != 0
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
        for (value in Contact.values().asSequence().filter { contacted.and(it.value) != 0 }) {
            when (value) {
                Contact.CALL -> permissions.add(Manifest.permission.READ_CALL_LOG)
                Contact.MESSAGE -> permissions.add(Manifest.permission.READ_SMS)
            }
        }
        return permissions.toTypedArray()
    }

    private fun requestRepeatedPermissions() {
        registerForRepeatedPermissions.launch(Manifest.permission.READ_CALL_LOG)
    }

    private fun requestMessagesPermissions() {
        registerForMessagesPermissions.launch(getMessagesPermissions())
    }

    private fun getMessagesPermissions(): Array<String> {
        val messages = prefs.messages
        val permissions = mutableListOf<String>()
        for (value in Message.values().asSequence().filter { messages.and(it.value) != 0 }) {
            when (value) {
                Message.ADDRESS -> permissions.add(Manifest.permission.READ_SMS)
                Message.BODY -> permissions.add(Manifest.permission.RECEIVE_SMS)
            }
        }
        return permissions.toTypedArray()
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
        return Utils.hasPermissions(this, *getMessagesPermissions())
    }

    private fun requestSimPermissions() {
        registerForSimPermissions.launch(Manifest.permission.READ_PHONE_STATE)
    }
}
