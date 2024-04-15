package me.lucky.silence.fragment

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

import me.lucky.silence.Contact
import me.lucky.silence.Message
import me.lucky.silence.Preferences
import me.lucky.silence.Utils
import me.lucky.silence.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private lateinit var ctx: Context
    private lateinit var prefs: Preferences
    private val roleManager: RoleManager by lazy { ctx.getSystemService(RoleManager::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        init()
        setup()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        update()
    }

    private fun init() {
        ctx = this.requireContext()
        prefs = Preferences(ctx)
        binding.apply {
            contacted.isChecked = prefs.isContactedChecked
            groups.isChecked = prefs.isGroupsChecked
            repeated.isChecked = prefs.isRepeatedChecked
            messages.isChecked = prefs.isMessagesChecked
            block.isChecked = prefs.isBlockEnabled
        }
    }

    private fun update() { binding.toggle.isChecked = prefs.isEnabled }

    private fun setup() = binding.apply {
        contacted.setOnCheckedChangeListener { _, isChecked ->
            prefs.isContactedChecked = isChecked
            if (isChecked) requestContactedPermissions()
        }
        groups.setOnCheckedChangeListener { _, isChecked ->
            prefs.isGroupsChecked = isChecked
        }
        repeated.setOnCheckedChangeListener { _, isChecked ->
            prefs.isRepeatedChecked = isChecked
            if (isChecked) requestRepeatedPermissions()
        }
        messages.setOnCheckedChangeListener { _, isChecked ->
            prefs.isMessagesChecked = isChecked
            if (isChecked) requestMessagesPermissions()
            Utils.updateMessagesTextEnabled(ctx)
        }
        block.setOnCheckedChangeListener { _, isChecked ->
            prefs.isBlockEnabled = isChecked
        }
        toggle.setOnCheckedChangeListener { _, isChecked ->
            prefs.isEnabled = isChecked
            if (isChecked) requestCallScreeningRole()
            Utils.updateMessagesTextEnabled(ctx)
        }
    }

    private fun requestContactedPermissions() =
        registerForContactedPermissions.launch(getContactedPermissions())

    private fun requestRepeatedPermissions() =
        registerForRepeatedPermissions.launch(Manifest.permission.READ_CALL_LOG)

    private fun requestMessagesPermissions() =
        registerForMessagesPermissions.launch(getMessagesPermissions())

    private fun requestCallScreeningRole() =
        registerForCallScreeningRole
            .launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))

    private fun getContactedPermissions(): Array<String> {
        val contacted = prefs.contacted
        val permissions = mutableListOf<String>()
        for (value in Contact.values().asSequence().filter { contacted.and(it.value) != 0 }) {
            permissions.add(when (value) {
                Contact.CALL -> Manifest.permission.READ_CALL_LOG
                Contact.MESSAGE -> Manifest.permission.READ_SMS
                Contact.ANSWER -> Manifest.permission.READ_CALL_LOG
            })
        }
        return permissions.toTypedArray()
    }

    private fun getMessagesPermissions(): Array<String> {
        val messages = prefs.messages
        val permissions = mutableListOf<String>()
        for (value in Message.values().asSequence().filter { messages.and(it.value) != 0 }) {
            when (value) {
                Message.INBOX -> permissions.add(Manifest.permission.READ_SMS)
                Message.TEXT -> {}
            }
        }
        return permissions.toTypedArray()
    }

    private val registerForContactedPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private val registerForRepeatedPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val registerForMessagesPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private val registerForCallScreeningRole =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
}