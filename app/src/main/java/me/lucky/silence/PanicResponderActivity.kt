package me.lucky.silence

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import info.guardianproject.panic.Panic
import info.guardianproject.panic.PanicResponder

class PanicResponderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (
            !Panic.isTriggerIntent(intent) ||
            !PanicResponder.receivedTriggerFromConnectedApp(this)
        ) {
            finish()
            return
        }
        AppDatabase.getInstance(this).smsFilterDao().deleteAll()
        finish()
    }
}
