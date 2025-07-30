package com.example.alarm_o_clock

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.Calendar


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Make sure to wrap with your app's theme
            // YourAppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = AppDestinations.ALARM_LIST_ROUTE) {
                    composable(AppDestinations.ALARM_LIST_ROUTE) {
                        AlarmListScreen(navController = navController)
                    }
                    composable(AppDestinations.SET_ALARM_ROUTE) {
                        SetAlarmScreen(navController = navController)
                    }
                }
            }
            // }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAlarmScreen(navController: NavController) { // Renamed from AlarmAppScreen, takes NavController
    val context = LocalContext.current // Use LocalContext for Composables

    // String states to hold the raw text input
    var hourString by remember {
        mutableStateOf(
            Calendar.getInstance().get(Calendar.HOUR).let { if (it == 0) 12 else it }.toString()
        )
    }
    var minuteString by remember {
        mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE).toString().padStart(2, '0'))
    }

    // Integer states for the actual validated alarm time
    var hour12 by remember {
        mutableStateOf(hourString.toIntOrNull() ?: Calendar.getInstance().get(Calendar.HOUR).let { if (it == 0) 12 else it })
    }
    var minute by remember {
        mutableStateOf(minuteString.toIntOrNull() ?: Calendar.getInstance().get(Calendar.MINUTE))
    }
    var isAm by remember { mutableStateOf(Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM) }

    LaunchedEffect(hourString) {
        val newHour = hourString.toIntOrNull()
        if (newHour != null && newHour in 1..12) {
            hour12 = newHour
        }
    }

    LaunchedEffect(minuteString) {
        val newMinute = minuteString.toIntOrNull()
        if (newMinute != null && newMinute in 0..59) {
            minute = newMinute
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Set New Alarm", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = hourString,
                onValueChange = { hStr ->
                    if (hStr.length <= 2 && hStr.all { it.isDigit() }) {
                        hourString = hStr
                        val currentValidHour = hStr.toIntOrNull()
                        if (currentValidHour != null && currentValidHour in 1..12) {
                            hour12 = currentValidHour
                        }
                    } else if (hStr.isEmpty()) {
                        hourString = ""
                    }
                },
                label = { Text("Hour (1-12)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = minuteString,
                onValueChange = { mStr ->
                    if (mStr.length <= 2 && mStr.all { it.isDigit() }) {
                        minuteString = mStr
                        val currentValidMinute = mStr.toIntOrNull()
                        if (currentValidMinute != null && currentValidMinute in 0..59) {
                            minute = currentValidMinute
                        }
                    } else if (mStr.isEmpty()) {
                        minuteString = ""
                    }
                },
                label = { Text("Minute (0-59)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("AM/PM: ")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = !isAm,
                onCheckedChange = { isPmChecked -> isAm = !isPmChecked }
            )
            Text(if (isAm) " AM" else " PM")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val finalHour = hourString.toIntOrNull()
                val finalMinute = minuteString.toIntOrNull()

                if (finalHour == null || finalHour !in 1..12) {
                    Toast.makeText(context, "Please enter a valid hour", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (finalMinute == null || finalMinute !in 0..59) {
                    Toast.makeText(context, "Please enter a valid minute", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        Intent().also { intent ->
                            intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                            context.startActivity(intent)
                        }
                        Toast.makeText(context, "Please grant permission to schedule exact alarms", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                }

                val hour24 = when {
                    isAm && finalHour == 12 -> 0
                    !isAm && finalHour < 12 -> finalHour + 12
                    else -> finalHour
                }

                // --- Integration with AlarmStorage and multiple alarms ---
                val newAlarmId = AlarmStorage.getNextAlarmId(context) // Assumes AlarmStorage.kt exists
                val newAlarm = AlarmItem(id = newAlarmId, hour = hour24, minute = finalMinute, isEnabled = true) // Assumes AlarmItem.kt exists

                val currentAlarms = AlarmStorage.loadAlarms(context)
                currentAlarms.add(newAlarm)
                AlarmStorage.saveAlarms(context, currentAlarms)

                setAlarm(context, newAlarm.id, newAlarm.hour, newAlarm.minute) // Call the version with alarmId
                // --- End integration ---

                Toast.makeText(context, "Alarm Added!", Toast.LENGTH_SHORT).show()
                navController.popBackStack() // Go back to the alarm list screen
            }
        ) {
            Text("Add Alarm")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.popBackStack() // Simple cancel button
        }) {
            Text("Cancel")
        }
    }
}

// Keep the setAlarm and cancelAlarm functions that take an alarmId
fun setAlarm(context: Context, alarmId: Int, hour: Int, minute: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("ALARM_ID", alarmId)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId, // Use the unique alarmId as the request code
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }
    }

    try {
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(calendar.timeInMillis, getAlarmInfoPendingIntent(context, alarmId)),
            pendingIntent
        )
        Toast.makeText(context, "Alarm set for ${String.format("%02d:%02d", hour, minute)}", Toast.LENGTH_SHORT).show()
        android.util.Log.d("MainActivity", "Alarm $alarmId set for: ${calendar.time}")

    } catch (e: SecurityException) {
        Toast.makeText(context, "Permission to schedule exact alarms not granted.", Toast.LENGTH_LONG).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent().also { settingsIntent ->
                settingsIntent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                context.startActivity(settingsIntent)
            }
        }
    }
}

fun cancelAlarm(context: Context, alarmId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarmId, // Must match the request code used in setAlarm
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
    Toast.makeText(context, "Alarm $alarmId cancelled", Toast.LENGTH_SHORT).show()
    android.util.Log.d("MainActivity", "Alarm $alarmId cancelled")

    // Optional: Stop a specific service if it's running for this alarm
    // val serviceIntent = Intent(context, AlarmService::class.java)
    // context.stopService(serviceIntent)
}

private fun getAlarmInfoPendingIntent(context: Context, alarmId: Int): PendingIntent {
    val alarmInfoIntent = Intent(context, MainActivity::class.java)
    alarmInfoIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
    return PendingIntent.getActivity(
        context,
        alarmId + 1000, // Ensure this request code is unique and different from broadcast PIs
        alarmInfoIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
