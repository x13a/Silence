package me.lucky.silence.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.lucky.silence.R

@Composable
fun ToggleableButton(
    getPreference: () -> Boolean,
    setPreference: (Boolean) -> Unit,
) {
    var isChecked by remember { mutableStateOf(getPreference()) }

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimension.PADDING),
        onClick = {
            isChecked = !isChecked
            setPreference(isChecked)
        },
        colors = if (isChecked) {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        } else {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        }
    ) {
        Text(
            text = if (isChecked) stringResource(R.string.toggle_on) else stringResource(R.string.toggle_off),
            style = MaterialTheme.typography.titleLarge,
            color = if (isChecked) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}