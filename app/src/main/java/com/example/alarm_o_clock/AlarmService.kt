package com.example.alarm_o_clock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import android.media.AudioManager

class AlarmService : android.app.Service() {

    private var mediaPlayer: android.media.MediaPlayer? = null // Make it nullable
    private val NOTIFICATION_ID = 123
    private val CHANNEL_ID = "AlarmServiceChannel"

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("AlarmService", "Service Created")
        // MediaPlayer will be initialized in onStartCommand
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1 // Get alarmId if passed
        android.util.Log.d("AlarmService", "Service Started. Alarm ID: $alarmId")

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("ALARM_ID_TO_DISMISS", alarmId) // Good to pass the ID
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            alarmId + 2000, // Use a unique request code based on alarmId
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm Ringing! (ID: $alarmId)")
            .setContentText("Tap to dismiss.")
            .setSmallIcon(R.drawable.ic_alarm_placeholder) // ENSURE THIS ICON EXISTS!
            .setContentIntent(pendingIntent)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)

        // On Android 12 (API 31) and above, foreground service launch restrictions apply.
        // The notification needs to be built correctly, and startForeground needs to be called promptly.
        try {
            val notification = notificationBuilder.build()
            startForeground(NOTIFICATION_ID, notification) // NOTIFICATION_ID could also be alarmId for uniqueness
            android.util.Log.d("AlarmService", "startForeground called successfully.")
        } catch (e: java.lang.Exception) {
            android.util.Log.e("AlarmService", "Error starting foreground service or building notification", e)
            // If startForeground fails, the service will likely be killed, leading to a crash or "App keeps stopping"
            stopSelf() // Stop the service if we can't go foreground
            return android.app.Service.START_NOT_STICKY
        }

        // Initialize and start MediaPlayer here
        if (mediaPlayer == null) {
            val defaultAlarmUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
            android.util.Log.d("AlarmService", "Default Alarm URI: $defaultAlarmUri")

            if (defaultAlarmUri == null) {
                android.util.Log.e("AlarmService", "Default alarm URI is null. Cannot play sound.")
                // Optionally play a fallback sound from raw resources here
                // Or simply don't play sound if no default is set
            } else {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(applicationContext, defaultAlarmUri)
                        // Consider requesting audio focus here (AudioManager) - good practice
                        // val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        // audioManager.requestAudioFocus(...)
                        isLooping = true
                        prepareAsync() // Use prepareAsync for network/content URIs
                        setOnPreparedListener { mp ->
                            Log.d("AlarmService", "MediaPlayer prepared. Starting playback.")
                            mp.start()
                        }
                        setOnErrorListener { mp, what, extra ->
                            Log.e("AlarmService", "MediaPlayer Error: what: $what, extra: $extra")
                            mp.release() // Release on error
                            mediaPlayer = null
                            true // Error was handled
                        }
                    }
                } catch (e: java.lang.Exception) {
                    android.util.Log.e("AlarmService", "Error setting up MediaPlayer", e)
                    mediaPlayer?.release() // Ensure release if partially setup
                    mediaPlayer = null
                }
            }
        } else if (mediaPlayer?.isPlaying == false) {
            try {
                mediaPlayer?.start() // If already prepared and just stopped
                android.util.Log.d("AlarmService", "MediaPlayer restarted.")
            } catch (e: java.lang.IllegalStateException) {
                android.util.Log.e("AlarmService", "Error restarting MediaPlayer", e)
                mediaPlayer?.release()
                mediaPlayer = null
                // Consider re-initializing if appropriate
            }
        }

        return androidx.core.app.ServiceCompat.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("AlarmService", "Service Destroyed")
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        // Release audio focus if you acquired it
    }

    override fun onBind(intent: android.content.Intent?): android.os.IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Service Channel Name", // User visible name
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Alarm Service notifications"
                setSound(null, null) // Service handles sound
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
            android.util.Log.d("AlarmService", "Notification channel created/ensured.")
        }
    }
}


