package me.lucky.silence

import android.app.role.RoleManager
import android.content.SharedPreferences
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService


class QSTileService : TileService() {
    private val roleManager by lazy { getSystemService(RoleManager::class.java) }
    private val prefs by lazy { Preferences(this) }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Preferences.SERVICE_ENABLED) updateTile()
    }

    override fun onClick() {
        super.onClick()
        prefs.isServiceEnabled = qsTile.state == Tile.STATE_INACTIVE
    }

    override fun onStartListening() {
        super.onStartListening()
        prefs.registerListener(prefsListener)
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        prefs.unregisterListener(prefsListener)
    }

    private fun updateTile() {
        qsTile.apply {
            state = when {
                !hasCallScreeningRole() -> Tile.STATE_UNAVAILABLE
                prefs.isServiceEnabled -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            }
            updateTile()
        }
    }

    private fun hasCallScreeningRole(): Boolean {
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }
}
