package com.example.alarm_o_clock

data class AlarmItem(
    val id: Int, // Unique ID for PendingIntent and list key
    val hour: Int, // 24-hour format
    val minute: Int,
    var isEnabled: Boolean = true // You might want to add this later
)
