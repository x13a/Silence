package me.lucky.silence

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import me.lucky.silence.ui.App

// Define a constant to identify the SEND_SMS permission request
private const val MY_PERMISSIONS_REQUEST_SEND_SMS = 0

open class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationManager(this).createNotificationChannels()

        // Check and request SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // If not, request the permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), MY_PERMISSIONS_REQUEST_SEND_SMS)
        }

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

    // This method is called when the user responds to the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            // If the result is for the SEND_SMS permission request
            MY_PERMISSIONS_REQUEST_SEND_SMS -> {
                // If the permission is granted
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Show a dialog to check SMS messages
                    AlertDialog.Builder(this)
                        .setTitle("Check SMS Messages")
                        .setMessage("Please check your SMS messages when you receive a notification.")
                        .setPositiveButton("OK") { dialog, _ ->
                            // Dismiss the dialog when the OK button is clicked
                            dialog.dismiss()
                        }
                        .show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }
}