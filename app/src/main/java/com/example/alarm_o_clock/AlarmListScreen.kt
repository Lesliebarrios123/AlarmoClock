package com.example.alarm_o_clock

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(navController: NavController) {
    val context = LocalContext.current
    var alarms by remember { mutableStateOf(AlarmStorage.loadAlarms(context)) }

    fun refreshAlarms() {
        alarms = AlarmStorage.loadAlarms(context)
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        if (navController.currentDestination?.route == AppDestinations.ALARM_LIST_ROUTE) {
            refreshAlarms()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(AppDestinations.SET_ALARM_ROUTE) }) { // Navigates to SetAlarmScreen
                Icon(Icons.Filled.Add, "Add new alarm")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your Alarms", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))

            if (alarms.isEmpty()) {
                Text("No alarms set. Tap the '+' button to add one.")
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmRow(
                            alarmItem = alarm,
                            onToggle = { updatedAlarm ->
                                val index = alarms.indexOfFirst { it.id == updatedAlarm.id }
                                if (index != -1) {
                                    alarms = alarms.toMutableList().apply { this[index] = updatedAlarm }
                                    AlarmStorage.saveAlarms(context, alarms)
                                    if (updatedAlarm.isEnabled) {
                                        setAlarm(context, updatedAlarm.id, updatedAlarm.hour, updatedAlarm.minute)
                                    } else {
                                        cancelAlarm(context, updatedAlarm.id)
                                    }
                                    refreshAlarms()
                                }
                            },
                            onDelete = { alarmToDelete ->
                                cancelAlarm(context, alarmToDelete.id)
                                alarms = alarms.filterNot { it.id == alarmToDelete.id }.toMutableList()
                                AlarmStorage.saveAlarms(context, alarms)
                                refreshAlarms()
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmRow(alarmItem: AlarmItem, onToggle: (AlarmItem) -> Unit, onDelete: (AlarmItem) -> Unit) {
    // ... (implementation of AlarmRow as previously defined)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = String.format("%02d:%02d", alarmItem.hour, alarmItem.minute),
                fontSize = 20.sp
            )
            Text(if (alarmItem.isEnabled) "Enabled" else "Disabled", fontSize = 12.sp)
        }
        Row {
            Switch(
                checked = alarmItem.isEnabled,
                onCheckedChange = { isChecked ->
                    onToggle(alarmItem.copy(isEnabled = isChecked))
                }
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onDelete(alarmItem) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        }
    }
}
