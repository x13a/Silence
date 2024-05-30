package me.lucky.silence.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.lucky.silence.Preferences
import me.lucky.silence.R
import me.lucky.silence.ui.common.Screen
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource

@Composable
fun TimerScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    var hoursInput by remember { mutableStateOf("") }
    var minutesInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val timerDescription = stringResource(R.string.timer_description)
    val context = LocalContext.current
    val timerStartedLabel = stringResource(R.string.timer_started_label)
    val startTimerLabel = stringResource(R.string.start_timer_label)
    val timerDoneAppOnLabel = stringResource(R.string.timer_done_app_on)
    val timerDoneAppOffLabel = stringResource(R.string.timer_done_app_off)

    Screen(title = R.string.timer, onBackPressed = onBackPressed) {
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            OutlinedTextField(
                value = hoursInput,
                onValueChange = { hoursInput = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.input_hours_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = minutesInput,
                onValueChange = { minutesInput = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.input_minutes_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                val hours = hoursInput.toIntOrNull() ?: 0
                val minutes = minutesInput.toIntOrNull() ?: 0
                coroutineScope.launch {
                    Toast.makeText(context, timerStartedLabel, Toast.LENGTH_SHORT).show()
                    delay((hours * 60L + minutes) * 60L * 1000L)
                    prefs.isEnabled = !prefs.isEnabled
                    val message = if (prefs.isEnabled) timerDoneAppOnLabel else timerDoneAppOffLabel
                    println(message)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(startTimerLabel)
            }
            Text(timerDescription)
        }
    }
}

@Preview
@Composable
fun TimerScreenPreview() {
    TimerScreen(Preferences(LocalContext.current)) { true }
}