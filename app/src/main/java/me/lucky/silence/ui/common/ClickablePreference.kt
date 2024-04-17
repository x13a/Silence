package me.lucky.silence.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ClickablePreference(
    name: String,
    description: String,
    onModuleClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = Dimension.HORIZONTAL_PADDING)
            .clickable(onClick = onModuleClick)
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
        }
    }
}