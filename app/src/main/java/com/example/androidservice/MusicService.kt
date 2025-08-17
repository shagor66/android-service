package com.example.androidservice

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicService: Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    private var mediaPlayer: MediaPlayer? = null

    private var serviceScope = CoroutineScope(Dispatchers.Default)

    private var updateJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action){
            ACTION.START.toString() -> {
                startMusic()
            }
            ACTION.STOP.toString() -> {
                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopMusic()
        super.onDestroy()
    }

    private fun startMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.audio)
            mediaPlayer?.isLooping = true
        }

        startForeground(1, buildNotification("Playing music..."))
        mediaPlayer?.start()

        updateJob = serviceScope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val position = mediaPlayer?.currentPosition ?: 0


                val minutes = (position / 1000) / 60
                val seconds = (position / 1000) % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                val notification = buildNotification("Playing: ${timeFormatted}s")
                getSystemService(NotificationManager::class.java).notify(1, notification)
                delay(1000)
            }
        }
    }

    private fun stopMusic() {
        updateJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopSelf()
    }

    private fun buildNotification(content: String): Notification {
        val stopIntent = Intent(this, MusicService::class.java).apply {
            action = ACTION.STOP.toString()
        }

        val stopPendingIntent = PendingIntent.getService(
            this,0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        return NotificationCompat.Builder(this, "running_channel")
            .setContentTitle("Music Player")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()
    }

    enum class ACTION{
        START, STOP
    }
}