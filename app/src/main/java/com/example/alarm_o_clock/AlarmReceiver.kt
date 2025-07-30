package com.example.alarm_o_clock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

class AlarmReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered!")
        val alarmId = intent.getIntExtra("ALARM_ID", -1) // Get the alarm ID
        Log.d("AlarmReceiver", "Alarm Fired! ID: $alarmId")
        // Start the service to play the alarm sound
        val serviceIntent = Intent(context, AlarmService::class.java)
        // You can pass data to the service if needed
        // serviceIntent.putExtra("ALARM_LABEL", "My Morning Alarm")
        context.startForegroundService(serviceIntent) // Use startForegroundService for API 26+
    }
}
