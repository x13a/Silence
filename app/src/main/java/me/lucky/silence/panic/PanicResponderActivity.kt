package me.lucky.silence.panic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import info.guardianproject.panic.Panic
import info.guardianproject.panic.PanicResponder
import me.lucky.silence.AppDatabase
import me.lucky.silence.Preferences
import me.lucky.silence.Utils

class PanicResponderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Panic.isTriggerIntent(intent)) {
            finishAndRemoveTask()
            return
        }
        Preferences(this).isEnabled = false
        Utils.setSmsReceiverState(this, false)
        if (PanicResponder.receivedTriggerFromConnectedApp(this))
            AppDatabase.getInstance(this).allowNumberDao().deleteAll()
        finishAndRemoveTask()
    }
}
