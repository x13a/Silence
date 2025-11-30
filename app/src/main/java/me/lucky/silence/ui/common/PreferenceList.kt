package me.lucky.silence.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

data class Preference(
    val getValue: () -> Boolean,
    val setValue: (Boolean) -> Unit,
    val name: Int,
    val description: Int,
    val dividerBefore: Boolean = false,
    val state: MutableState<Boolean>? = null,
)

@Composable
fun PreferenceList(preferenceList: List<Preference>) {
    Column(modifier = Modifier.padding(Dimension.LIST_PADDING)) {
        for (preference in preferenceList) {
            if (preference.dividerBefore) {
                HorizontalDivider(modifier = Modifier.padding(vertical = Dimension.PADDING))
            }
            PreferenceItem(preference)
        }
    }
}

@Composable
fun PreferenceItem(
    preference: Preference
) {
    val state = preference.state ?: remember { mutableStateOf(preference.getValue()) }
    var checkedState by state

    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Dimension.PADDING)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(preference.name),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(preference.description),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(modifier = Modifier.padding(Dimension.PADDING))
        Switch(checked = checkedState, onCheckedChange = { isChecked ->
            checkedState = isChecked
            preference.setValue(isChecked)
        })
    }
}
