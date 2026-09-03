package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.DownloadEntity
import com.example.data.local.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observeJob: Job? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var notificationManager: NotificationManager? = null

    companion object {
        const val CHANNEL_ID = "omnistream_background_downloads"
        const val CHANNEL_NAME = "OmniStream Downloads"
        const val NOTIFICATION_ID = 4040

        const val ACTION_START_SERVICE = "com.example.service.action.START_DOWNLOAD"
        const val ACTION_STOP_SERVICE = "com.example.service.action.STOP_DOWNLOAD"

        fun start(context: Context) {
            try {
                val intent = Intent(context, DownloadForegroundService::class.java).apply {
                    action = ACTION_START_SERVICE
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, DownloadForegroundService::class.java).apply {
                    action = ACTION_STOP_SERVICE
                }
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        // Acquire WakeLock to keep CPU running when screen is off or app is minimized
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "OmniStream:BackgroundDownloadWakeLock"
            )?.apply {
                setReferenceCounted(false)
            }
        } catch (_: Exception) {}

        // Acquire WifiLock to prevent Wi-Fi power saving / sleep disconnect
        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiLock = wifiManager?.createWifiLock(
                WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                "OmniStream:BackgroundDownloadWifiLock"
            )?.apply {
                setReferenceCounted(false)
            }
        } catch (_: Exception) {}

        // Initial foreground notification to satisfy Android OS SLA
        val initialNotification = buildNotification(
            title = "OmniStream Media Engine",
            content = "Background download service active",
            progress = 0,
            isIndeterminate = true
        )

        val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                initialNotification,
                foregroundServiceType
            )
        } catch (_: Exception) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForegroundAndSelf()
            return START_NOT_STICKY
        }

        startObservingActiveDownloads()
        return START_STICKY
    }

    private fun startObservingActiveDownloads() {
        if (observeJob != null && observeJob?.isActive == true) return

        observeJob = serviceScope.launch {
            val dao = AppDatabase.getInstance(applicationContext).downloadDao()
            dao.getActiveDownloads().collectLatest { activeList ->
                if (activeList.isNotEmpty()) {
                    acquireLocks()
                    updateForegroundNotification(activeList)
                } else {
                    // Give a 2-second grace period in case the next download in queue is starting
                    delay(2000)
                    val stillActive = dao.getActiveDownloads()
                    // If still empty after grace period, stop service cleanly
                    releaseLocks()
                    stopForegroundAndSelf()
                }
            }
        }
    }

    private fun acquireLocks() {
        try {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(30 * 60 * 1000L) // 30 min safety timeout
            }
        } catch (_: Exception) {}

        try {
            if (wifiLock?.isHeld == false) {
                wifiLock?.acquire()
            }
        } catch (_: Exception) {}
    }

    private fun releaseLocks() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}

        try {
            if (wifiLock?.isHeld == true) {
                wifiLock?.release()
            }
        } catch (_: Exception) {}
    }

    private fun updateForegroundNotification(activeList: List<DownloadEntity>) {
        val current = activeList.firstOrNull { it.status == DownloadStatus.DOWNLOADING } ?: activeList.first()
        val percent = current.progressPercent.coerceIn(0, 100)
        val speed = current.downloadSpeedText.ifBlank { "Downloading..." }
        val eta = current.etaText.ifBlank { "--" }

        val title = if (activeList.size > 1) {
            "[${activeList.size} Active] ${current.title.take(30)}"
        } else {
            current.title.take(36)
        }

        val content = "$percent% • $speed • ETA: $eta"

        val notification = buildNotification(
            title = title,
            content = content,
            progress = percent,
            isIndeterminate = percent <= 0
        )

        notificationManager?.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(
        title: String,
        content: String,
        progress: Int,
        isIndeterminate: Boolean
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_download)
            .setContentTitle(title)
            .setContentText(content)
            .setProgress(100, progress, isIndeterminate)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live download progress, speed, and status for OmniStream background downloads"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun stopForegroundAndSelf() {
        releaseLocks()
        try {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {}
        stopSelf()
    }

    override fun onDestroy() {
        releaseLocks()
        observeJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
