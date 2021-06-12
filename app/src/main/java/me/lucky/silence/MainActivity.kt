package me.lucky.silence

import android.app.Activity
import android.app.role.RoleManager
import android.os.Bundle
import android.widget.Button

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val roleManager by lazy { getSystemService(RoleManager::class.java) }
    private val prefs by lazy { Preferences(this) }

    private val getRole =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            toggle()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn: Button = findViewById(R.id.toggle)
        if (prefs.isServiceEnabled) {
            btn.text = getText(R.string.toggle_on)
            btn.backgroundTintList = getColorStateList(R.color.red)
        }
        btn.setOnClickListener {
            if (
                !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) &&
                !prefs.isServiceEnabled
            ) {
                getRole.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
            } else {
                toggle()
            }
        }
    }

    private fun toggle() {
        val isEnabled = !prefs.isServiceEnabled
        prefs.isServiceEnabled = isEnabled
        val stringId: Int
        val colorId: Int
        if (isEnabled) {
            stringId = R.string.toggle_on
            colorId = R.color.red
        } else {
            stringId = R.string.toggle_off
            colorId = R.color.green
        }
        val btn: Button = findViewById(R.id.toggle)
        btn.text = getText(stringId)
        btn.backgroundTintList = getColorStateList(colorId)
    }
}
