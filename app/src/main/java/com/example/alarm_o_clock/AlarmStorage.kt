package com.example.alarm_o_clock

import android.content.Context
import android.content.SharedPreferences

// For simplicity, storing as comma-separated strings: "id,hour,minute,isEnabled"
// A more robust solution would use JSON with Gson or a database.

object AlarmStorage {
    private const val PREFS_NAME = "AlarmClockPrefs"
    private const val ALARMS_KEY = "alarms"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveAlarms(context: Context, alarms: List<AlarmItem>) {
        val editor = getPreferences(context).edit()
        // Convert List<AlarmItem> to a Set<String>
        val alarmStrings = alarms.map { "${it.id},${it.hour},${it.minute},${it.isEnabled}" }.toSet()
        editor.putStringSet(ALARMS_KEY, alarmStrings)
        editor.apply()
    }

    fun loadAlarms(context: Context): MutableList<AlarmItem> {
        val savedStrings = getPreferences(context).getStringSet(ALARMS_KEY, emptySet()) ?: emptySet()
        return savedStrings.mapNotNull { str ->
            try {
                val parts = str.split(',')
                AlarmItem(
                    id = parts[0].toInt(),
                    hour = parts[1].toInt(),
                    minute = parts[2].toInt(),
                    isEnabled = parts.getOrNull(3)?.toBoolean() ?: true // Handle older format
                )
            } catch (e: Exception) {
                null // Skip malformed entries
            }
        }.toMutableList()
    }

    // Helper to get a new unique ID
    fun getNextAlarmId(context: Context): Int {
        val alarms = loadAlarms(context)
        return (alarms.maxOfOrNull { it.id } ?: 0) + 1
    }
}
