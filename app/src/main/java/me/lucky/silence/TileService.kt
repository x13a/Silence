package me.lucky.silence

import android.content.SharedPreferences
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class TileService : TileService() {
    private lateinit var prefs: Preferences

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Preferences.SERVICE_ENABLED) update()
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Preferences(this)
    }

    override fun onClick() {
        super.onClick()
        val state = qsTile.state == Tile.STATE_INACTIVE
        prefs.isServiceEnabled = state
        Utils.setSmsReceiverState(
            this,
            state && prefs.isMessagesChecked && prefs.messages.and(Message.BODY.value) != 0,
        )
    }

    override fun onStartListening() {
        super.onStartListening()
        prefs.registerListener(prefsListener)
        update()
    }

    override fun onStopListening() {
        super.onStopListening()
        prefs.unregisterListener(prefsListener)
    }

    private fun update() {
        qsTile.state = when {
            !Utils.hasCallScreeningRole(this) -> Tile.STATE_UNAVAILABLE
            prefs.isServiceEnabled -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        qsTile.updateTile()
    }
}
