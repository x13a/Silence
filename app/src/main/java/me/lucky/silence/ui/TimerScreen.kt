package me.lucky.silence.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import kotlinx.coroutines.Job
import androidx.compose.material3.LinearProgressIndicator

// TimerScreen Composable function
@Composable
fun TimerScreen(prefs: Preferences, onBackPressed: () -> Boolean) {
    // Define mutable states
    var hoursInput by remember { mutableStateOf("") }
    var minutesInput by remember { mutableStateOf("") }
    var remainingTime by remember { mutableStateOf("") }
    var totalSeconds by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    var timerJob by remember { mutableStateOf<Job?>(null) }

    // Define string resources
    val timerDescription = stringResource(R.string.timer_description)
    val context = LocalContext.current
    val timerStartedLabel = stringResource(R.string.timer_started_label)
    val startTimerLabel = stringResource(R.string.start_timer_label)
    val cancelTimerLabel = stringResource(R.string.cancel_timer_label)
    val timerDoneAppOnLabel = stringResource(R.string.timer_done_app_on)
    val timerDoneAppOffLabel = stringResource(R.string.timer_done_app_off)

    // Define Screen Composable
    Screen(title = R.string.timer, onBackPressed = onBackPressed) {
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            // Define OutlinedTextField for hours input
            OutlinedTextField(
                value = hoursInput,
                onValueChange = { hoursInput = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.input_hours_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            // Define OutlinedTextField for minutes input
            OutlinedTextField(
                value = minutesInput,
                onValueChange = { minutesInput = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.input_minutes_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            // Define Button to start timer
            Button(onClick = {
                val hours = hoursInput.toIntOrNull() ?: 0
                val minutes = minutesInput.toIntOrNull() ?: 0
                totalSeconds = (hours * 60 + minutes) * 60
                timerJob?.cancel()
                timerJob = coroutineScope.launch {
                    Toast.makeText(context, timerStartedLabel, Toast.LENGTH_SHORT).show()
                    for (i in totalSeconds downTo 0) {
                        remainingTime = "${i / 3600}:${(i % 3600) / 60}:${i % 60}"
                        delay(1000L)
                    }
                    prefs.isEnabled = !prefs.isEnabled
                    val message = if (prefs.isEnabled) timerDoneAppOnLabel else timerDoneAppOffLabel
                    println(message)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(startTimerLabel)
            }
            // Define Button to cancel timer
            Button(onClick = {
                timerJob?.cancel()
                remainingTime = ""
            }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(cancelTimerLabel)
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Define LinearProgressIndicator to show timer progress
            if (remainingTime.isNotEmpty() && totalSeconds > 0) {
                val remainingSeconds = remainingTime.split(":").map { it.toInt() }.reduce { acc, i -> acc * 60 + i }
                LinearProgressIndicator(progress = 1f - remainingSeconds.toFloat() / totalSeconds, modifier = Modifier.fillMaxWidth())
            }
            // Define Text to show remaining time
            if (remainingTime.isNotEmpty()) {
                Text(stringResource(R.string.remaining_time, remainingTime))
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Define Text to show timer description
            Text(timerDescription)
        }
    }
}

// Define Preview Composable for TimerScreen
@Preview
@Composable
fun TimerScreenPreview() {
    TimerScreen(Preferences(LocalContext.current)) { true }
}