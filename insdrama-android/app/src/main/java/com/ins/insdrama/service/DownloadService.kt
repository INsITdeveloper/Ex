package com.ins.insdrama.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ins.insdrama.R
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : Service() {

    companion object {
        const val CHANNEL_ID = "insdrama_download_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_URL = "extra_url"
        const val EXTRA_FILENAME = "extra_filename"
        const val EXTRA_EPISODE = "extra_episode"
    }

    private var downloadThread: Thread? = null
    @Volatile
    private var isDownloading = false
    @Volatile
    private var shouldStop = false

    private var currentBookId: String? = null
    private var currentDramaTitle: String? = null
    private var currentEpisodeIndex: Int = -1
    private var currentCoverUrl: String? = null
    private var currentSynopsis: String? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(EXTRA_URL)
        val filename = intent?.getStringExtra(EXTRA_FILENAME)
        val episodeInfo = intent?.getStringExtra(EXTRA_EPISODE)

        currentBookId = intent?.getStringExtra("bookId")
        currentDramaTitle = intent?.getStringExtra("dramaTitle")
        currentEpisodeIndex = intent?.getIntExtra("episodeIndex", -1) ?: -1
        currentCoverUrl = intent?.getStringExtra("coverUrl")
        currentSynopsis = intent?.getStringExtra("synopsis")

        if (url != null && filename != null) {
            startDownload(url, filename, episodeInfo ?: "")
        } else {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Download Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows download progress"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startDownload(url: String, filename: String, episodeInfo: String) {
        if (isDownloading) {
            stopSelf()
            return
        }
        isDownloading = true
        shouldStop = false

        val notification = createNotification(0, episodeInfo)
        startForeground(NOTIFICATION_ID, notification)

        downloadThread = Thread {
            var connection: HttpURLConnection? = null
            var output: FileOutputStream? = null

            try {
                connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.setRequestProperty("Accept", "*/*")
                connection.connect()

                val fileLength = connection.contentLength.toLong()
                val downloadDir = File(getExternalFilesDir(null), "downloads")
                if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                    throw RuntimeException("Cannot create download directory")
                }

                val file = File(downloadDir, filename)
                val input = connection.inputStream
                output = FileOutputStream(file)

                val data = ByteArray(8192)
                var total: Long = 0
                var count: Int
                var lastProgress = -1

                while (input.read(data).also { count = it } != -1 && !shouldStop) {
                    total += count.toLong()
                    val progress = if (fileLength > 0) {
                        ((total * 100) / fileLength).toInt()
                    } else 0

                    if (progress != lastProgress && progress % 5 == 0) {
                        updateNotification(progress, episodeInfo)
                        lastProgress = progress
                    }

                    output.write(data, 0, count)
                }

                output.flush()

                if (shouldStop) {
                    file.delete()
                } else {
                    // Mark as complete in DownloadManager
                    if (currentBookId != null && currentDramaTitle != null) {
                        com.ins.insdrama.util.DownloadManager.markDownloadComplete(
                            this@DownloadService,
                            currentBookId!!,
                            currentDramaTitle!!,
                            currentEpisodeIndex,
                            currentCoverUrl ?: "",
                            currentSynopsis ?: ""
                        )
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }
                    showCompleteNotification(episodeInfo)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                val partialFile = File(getExternalFilesDir(null), "downloads/$filename")
                if (partialFile.exists()) partialFile.delete()
                stopForeground(STOP_FOREGROUND_REMOVE)
                showErrorNotification("Download gagal: ${e.message}")

            } finally {
                try {
                    output?.close()
                } catch (_: Exception) {}
                try {
                    connection?.disconnect()
                } catch (_: Exception) {}
                isDownloading = false
                stopSelf()
            }
        }
        downloadThread?.start()
    }

    private fun createNotification(progress: Int, episodeInfo: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading...")
            .setContentText(episodeInfo)
            .setSmallIcon(R.drawable.ic_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(progress: Int, episodeInfo: String) {
        val notification = createNotification(progress, episodeInfo)
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private fun showCompleteNotification(episodeInfo: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Selesai")
            .setContentText(episodeInfo)
            .setSmallIcon(R.drawable.ic_done)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID + 1, notification)
    }

    private fun showErrorNotification(message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download Gagal")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_close)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID + 2, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        shouldStop = true
        downloadThread?.interrupt()
    }
}
