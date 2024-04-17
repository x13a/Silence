package me.lucky.silence.panic

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import info.guardianproject.panic.PanicResponder
import me.lucky.silence.MainActivity
import me.lucky.silence.R

class PanicConnectionActivity : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PanicResponder.checkForDisconnectIntent(this)) {
            finish()
            return
        }
        val sender = PanicResponder.getConnectIntentSender(this)
        val packageName = PanicResponder.getTriggerPackageName(this)
        if (sender != "" && sender != packageName)
            setContent { ShowOptInDialog(this) } else finish()
    }
}

@Composable
fun ShowOptInDialog(ctx: PanicConnectionActivity) {
    var app: CharSequence = ctx.getString(R.string.panic_app_unknown_app)
    val packageName = ctx.callingActivity?.packageName
    if (packageName != null) {
        try {
            app = ctx.packageManager
                .getApplicationLabel(ctx.packageManager.getApplicationInfo(packageName, 0))
        } catch (_: PackageManager.NameNotFoundException) {}
    }
    AlertDialog(
        onDismissRequest = {
            ctx.setResult(Activity.RESULT_CANCELED)
            ctx.finish()
        },
        confirmButton = {
            TextButton(onClick = {
                PanicResponder.setTriggerPackageName(ctx)
                ctx.setResult(Activity.RESULT_OK)
                ctx.finish()
            }) { Text(text = stringResource(id = R.string.allow)) }
        },
        dismissButton = {
            TextButton(onClick = {
                ctx.setResult(Activity.RESULT_CANCELED)
                ctx.finish()
            }) { Text(text = stringResource(id = android.R.string.cancel)) }
        },
        title = { Text(text = stringResource(id = R.string.panic_app_dialog_title)) },
        text = { Text(text = stringResource(id = R.string.panic_app_dialog_message, app)) },
    )
}