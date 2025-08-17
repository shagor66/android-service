package com.example.androidservice

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RunningServices : Service() {

    private var seconds = 0

    private var serviceJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> {
             start()
            }

            Actions.STOP.toString() -> {
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        super.onDestroy()
    }

    private fun start() {
        startForeground(1, buildNotification("Timer started: 0s"))

        serviceJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                seconds++
                val notification = buildNotification("Timer: ${seconds}s")
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(1, notification)
            }
        }
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, "running_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Stopwatch Running")
            .setContentTitle(content)
            .build()
    }

    enum class Actions {
        START, STOP
    }
}