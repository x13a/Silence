package me.lucky.silence

import android.app.role.RoleManager
import android.content.SharedPreferences
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService


class QSTileService : TileService() {
    private val roleManager by lazy { getSystemService(RoleManager::class.java) }
    private val prefs by lazy { Preferences(this) }

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Preferences.SERVICE_ENABLED) {
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        when (qsTile.state) {
            Tile.STATE_INACTIVE -> {
                if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    prefs.isServiceEnabled = true
                }
            }
            Tile.STATE_ACTIVE -> prefs.isServiceEnabled = false
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        prefs.register(listener)
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        prefs.unregister(listener)
    }

    private fun updateTile() {
        qsTile.apply {
            state = if (prefs.isServiceEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }
}
