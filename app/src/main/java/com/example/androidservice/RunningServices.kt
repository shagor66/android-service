package com.example.androidservice

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class RunningServices: Service() {

    private var seconds = 0
    private var isRunning = false
    private lateinit var timerThread: Thread


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        isRunning = false
        if (::timerThread.isInitialized){
            timerThread.interrupt()
        }

        super.onDestroy()
    }

    private fun start(){
        isRunning = true

        startForeground(1,buildNotification("Timer started: 0s"))


        timerThread = Thread {
            try {
                while (isRunning) {
                    Thread.sleep(1000)
                    seconds++
                    val notification = buildNotification("Timer: ${seconds}s")
                    val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(1, notification)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        timerThread.start()
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this,"running_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Stopwatch Running")
            .setContentTitle(content)
            .build()
    }

    enum class Actions{
        START,STOP
    }
}