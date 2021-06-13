package me.lucky.silence

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
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

    private val requestCallScreening =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            toggle(true)
        }
    }
    private val requestCallLog =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                prefs.isCallbackChecked = true
            } else {
                binding.callbackSwitch.isChecked = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUi()
    }

    private fun initUi() {
        with (binding.callbackSwitch) {
            isChecked = prefs.isCallbackChecked
            setOnCheckedChangeListener { _, isChecked ->
                if (
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.READ_CALL_LOG,
                    ) != PackageManager.PERMISSION_GRANTED &&
                    isChecked
                ) {
                    requestCallLog.launch(Manifest.permission.READ_CALL_LOG)
                } else {
                    prefs.isCallbackChecked = isChecked
                }
            }
        }

        with (binding.tollFreeSwitch) {
            isChecked = prefs.isTollFreeChecked
            setOnCheckedChangeListener { _, isChecked -> prefs.isTollFreeChecked = isChecked }
        }

        toggle(prefs.isServiceEnabled)
        binding.toggle.setOnClickListener {
            val isNotEnabled = !prefs.isServiceEnabled
            if (
                !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) &&
                isNotEnabled
            ) {
                requestCallScreening.launch(
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
            } else {
                toggle(isNotEnabled)
            }
        }
    }

    private fun toggle(isChecked: Boolean) {
        prefs.isServiceEnabled = isChecked
        val stringId: Int
        val colorId: Int
        if (isChecked) {
            stringId = R.string.toggle_on
            colorId = R.color.red
        } else {
            stringId = R.string.toggle_off
            colorId = R.color.green
        }
        with (binding.toggle) {
            text = getText(stringId)
            backgroundTintList = getColorStateList(colorId)
        }
    }
}
