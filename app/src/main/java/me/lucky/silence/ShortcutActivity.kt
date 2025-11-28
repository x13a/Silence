package me.lucky.silence

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

class ShortcutActivity : Activity() {
    companion object {
        const val PREFIX = "me.lucky.silence.action"
        const val ACTION_ACTIVATE = "$PREFIX.ACTIVATE"
        const val ACTION_DEACTIVATE = "$PREFIX.DEACTIVATE"
        const val ACTION_ACTIVATE_BLOCK = "$PREFIX.ACTIVATE_BLOCK"
        const val ACTION_DEACTIVATE_BLOCK = "$PREFIX.DEACTIVATE_BLOCK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent?.action) {
            ACTION_ACTIVATE -> activate()
            ACTION_DEACTIVATE -> deactivate()
            ACTION_ACTIVATE_BLOCK -> activateBlock()
            ACTION_DEACTIVATE_BLOCK -> deactivateBlock()
        }
        finish()
    }

    private fun activate() {
        val prefs = Preferences(this)
        prefs.isEnabled = true
        Utils.updateMessagesEnabled(this)
        Toast.makeText(
            this,
            getString(R.string.shortcut_activate),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun deactivate() {
        val prefs = Preferences(this)
        prefs.isEnabled = false
        Utils.updateMessagesEnabled(this)
        Toast.makeText(
            this,
            getString(R.string.shortcut_deactivate),
            Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun activateBlock() {
        val prefs = Preferences(this)
        prefs.isEnabled = true
        prefs.isBlockEnabled = true
        // Update messages functionality when global state changes
        Utils.updateMessagesEnabled(this)
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
        // Update messages functionality when global state changes
        Utils.updateMessagesEnabled(this)
        Toast.makeText(
            this, 
            getString(R.string.shortcut_deactivate_block), 
            Toast.LENGTH_SHORT
        ).show()
    }
}
