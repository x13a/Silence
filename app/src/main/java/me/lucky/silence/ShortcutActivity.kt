package me.lucky.silence

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class ShortcutActivity : Activity() {
    companion object {
        const val ACTION_ACTIVATE_BLOCK = "me.lucky.silence.action.ACTIVATE_BLOCK"
        const val ACTION_DEACTIVATE_BLOCK = "me.lucky.silence.action.DEACTIVATE_BLOCK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        when (intent?.action) {
            ACTION_ACTIVATE_BLOCK -> {
                activateBlock()
            }
            ACTION_DEACTIVATE_BLOCK -> {
                deactivateBlock()
            }
        }
        
        finish()
    }
    
    private fun activateBlock() {
        val prefs = Preferences(this)
        prefs.isEnabled = true
        prefs.isBlockEnabled = true
        
        // Update messages text functionality when global state changes
        Utils.updateMessagesTextEnabled(this)
        
        Toast.makeText(
            this, 
            getString(R.string.shortcut_activate_block), 
            Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun deactivateBlock() {
        val prefs = Preferences(this)
        prefs.isEnabled = false
        prefs.isBlockEnabled = false
        
        // Update messages text functionality when global state changes
        Utils.updateMessagesTextEnabled(this)
        
        Toast.makeText(
            this, 
            getString(R.string.shortcut_deactivate_block), 
            Toast.LENGTH_SHORT
        ).show()
    }
}
