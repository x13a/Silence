package me.lucky.silence.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SwitchPreference(
    name: String,
    description: String,
    getIsEnabled: () -> Boolean,
    setIsEnabled: (Boolean) -> Unit,
) {
    var switchState by remember { mutableStateOf(getIsEnabled()) }

    Surface(
        modifier = Modifier
            .padding(horizontal = Dimension.HORIZONTAL_PADDING)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Dimension.PADDING)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Switch(
                checked = switchState, onCheckedChange = { newSwitchState ->
                    switchState = newSwitchState
                    setIsEnabled(newSwitchState)
                }, modifier = Modifier.padding(horizontal = Dimension.HORIZONTAL_PADDING)
            )
        }
    }
}