package me.lucky.silence

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.navigation.compose.rememberNavController
import me.lucky.silence.ui.App

open class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Run migration to fix any incorrect defaults
        val prefs = Preferences(this)
        prefs.runMigrationIfNeeded()
        
        NotificationManager(this).createNotificationChannels()
        setContent {
            val isAndroid12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val colorScheme = when {
                isAndroid12OrLater && isSystemInDarkTheme() -> dynamicDarkColorScheme(this)
                isAndroid12OrLater -> dynamicLightColorScheme(this)
                isSystemInDarkTheme() -> darkColorScheme()
                else -> lightColorScheme()
            }
            MaterialTheme(colorScheme = colorScheme) {
                App(ctx = this, navController = rememberNavController())
            }
        }
    }
}
