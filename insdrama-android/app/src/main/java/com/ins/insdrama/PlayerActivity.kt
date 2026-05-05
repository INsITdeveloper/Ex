package com.ins.insdrama

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ins.insdrama.databinding.ActivityPlayerBinding
import com.ins.insdrama.model.Drama
import com.ins.insdrama.model.Episode
import com.ins.insdrama.util.DownloadManager
import java.io.File

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var exoPlayer: ExoPlayer? = null
    private var drama: Drama? = null
    private var currentEpisodeIndex = 0
    private var isSwitchingEpisode = false

    companion object {
        const val EXTRA_DRAMA = "extra_drama"
        const val EXTRA_EPISODE_INDEX = "extra_episode_index"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        drama = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_DRAMA, Drama::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_DRAMA)
        }
        currentEpisodeIndex = intent.getIntExtra(EXTRA_EPISODE_INDEX, 0)

        // Set orientation based on user preference
        val prefs = getSharedPreferences("insdrama_prefs", 0)
        val orientation = prefs.getString("orientation", "auto")
        requestedOrientation = when (orientation) {
            "portrait" -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            "landscape" -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        setupPlayer()
        setupControls()
        setupGesture()

        if (drama != null) {
            loadEpisode(currentEpisodeIndex)
            // Save to history
            saveToHistory()
        }
    }

    private fun setupPlayer() {
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_ENDED -> {
                            playNextEpisode()
                        }
                        Player.STATE_READY -> {
                            isSwitchingEpisode = false
                        }
                        Player.STATE_BUFFERING -> {
                            // Show buffering indicator
                        }
                        Player.STATE_IDLE -> {
                            // Player idle
                        }
                    }
                }
            })
        }
        binding.playerView.player = exoPlayer
    }

    private fun setupControls() {
        binding.backButton.setOnClickListener { finish() }

        binding.detailButton.setOnClickListener {
            val detailIntent = Intent(this, DetailActivity::class.java)
            detailIntent.putExtra(DetailActivity.EXTRA_DRAMA, drama)
            startActivity(detailIntent)
        }

        // Use ExoPlayer's built-in controls - hide custom buttons
        binding.playPauseButton.visibility = View.GONE
        binding.previousButton.visibility = View.GONE
        binding.nextButton.visibility = View.GONE

        // Handle double tap for seek
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val screenWidth = resources.displayMetrics.widthPixels.toFloat()
                if (e.x < screenWidth / 3) {
                    // Double tap left - rewind 10s
                    exoPlayer?.seekTo((exoPlayer?.currentPosition ?: 0L) - 10000)
                } else if (e.x > 2 * screenWidth / 3) {
                    // Double tap right - forward 10s
                    exoPlayer?.seekTo((exoPlayer?.currentPosition ?: 0L) + 10000)
                }
                return true
            }
        })

        binding.playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun setupGesture() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                // Swipe up/down for volume/brightness could be added here
                return false
            }
        })

        binding.playerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun loadEpisode(index: Int) {
        val d = drama ?: return
        if (index < 0 || index >= d.episodes.size) return

        isSwitchingEpisode = true
        val episode = d.episodes[index]

        binding.dramaTitleText.text = d.title
        binding.episodeInfoText.text = "Episode ${episode.index} dari ${d.episodes.size}"

        val localPath = DownloadManager.getLocalFilePath(this, d.bookId, episode)

        val mediaItem = if (localPath != null && File(localPath).exists()) {
            MediaItem.fromUri(Uri.fromFile(File(localPath)))
        } else {
            MediaItem.fromUri(episode.videoUrl)
        }

        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true

        isSwitchingEpisode = false
    }

    private fun playNextEpisode() {
        val d = drama ?: return
        if (currentEpisodeIndex < d.episodes.size - 1) {
            currentEpisodeIndex++
            loadEpisode(currentEpisodeIndex)
            saveToHistory()
        } else {
            playNextDrama()
        }
    }

    private fun playNextDrama() {
        exoPlayer?.stop()
        finish()
    }

    private fun saveToHistory() {
        val d = drama ?: return
        val prefs = getSharedPreferences("insdrama_history", 0)
        val historyJson = prefs.getString("history", "[]") ?: "[]"

        val historyList = try {
            val jsonArray = org.json.JSONArray(historyJson)
            val list = mutableListOf<HistoryItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(HistoryItem(
                    bookId = obj.getString("bookId"),
                    title = obj.getString("title"),
                    coverUrl = obj.getString("coverUrl"),
                    episodeIndex = obj.getInt("episodeIndex"),
                    watchedAt = obj.getLong("watchedAt")
                ))
            }
            list
        } catch (e: Exception) {
            mutableListOf()
        }

        // Remove existing entry for this drama if exists
        historyList.removeAll { it.bookId == d.bookId }

        // Add new entry at the beginning
        historyList.add(0, HistoryItem(
            bookId = d.bookId,
            title = d.title,
            coverUrl = d.coverUrl,
            episodeIndex = currentEpisodeIndex,
            watchedAt = System.currentTimeMillis()
        ))

        // Keep only last 50 items
        while (historyList.size > 50) {
            historyList.removeAt(historyList.size - 1)
        }

        // Save back to SharedPreferences
        val newJsonArray = org.json.JSONArray()
        historyList.forEach { item ->
            val obj = org.json.JSONObject()
            obj.put("bookId", item.bookId)
            obj.put("title", item.title)
            obj.put("coverUrl", item.coverUrl)
            obj.put("episodeIndex", item.episodeIndex)
            obj.put("watchedAt", item.watchedAt)
            newJsonArray.put(obj)
        }

        prefs.edit().putString("history", newJsonArray.toString()).apply()
    }

    data class HistoryItem(
        val bookId: String,
        val title: String,
        val coverUrl: String,
        val episodeIndex: Int,
        val watchedAt: Long
    )

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(android.view.WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }
}
