package me.lucky.silence

import android.content.SharedPreferences
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class QSTileService : TileService() {
    private val prefs by lazy { Preferences(this) }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Preferences.SERVICE_ENABLED) update()
    }

    override fun onClick() {
        super.onClick()
        prefs.isServiceEnabled = qsTile.state == Tile.STATE_INACTIVE
        Utils.setSmsReceiverState(this, prefs.isServiceEnabled && prefs.isSmsChecked)
    }

    override fun onStartListening() {
        super.onStartListening()
        update()
        prefs.registerListener(prefsListener)
    }

    override fun onStopListening() {
        super.onStopListening()
        prefs.unregisterListener(prefsListener)
    }

    private fun update() {
        qsTile.apply {
            state = when {
                !Utils.hasCallScreeningRole(this@QSTileService) -> Tile.STATE_UNAVAILABLE
                prefs.isServiceEnabled -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            }
            updateTile()
        }
    }
}
