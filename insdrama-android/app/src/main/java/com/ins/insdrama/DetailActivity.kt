package com.ins.insdrama

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ins.insdrama.adapter.EpisodeAdapter
import com.ins.insdrama.databinding.ActivityDetailBinding
import com.ins.insdrama.model.Drama
import com.ins.insdrama.model.Episode
import com.ins.insdrama.util.DownloadManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var episodeAdapter: EpisodeAdapter
    private var drama: Drama? = null

    companion object {
        const val EXTRA_DRAMA = "extra_drama"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drama = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_DRAMA, Drama::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_DRAMA)
        }

        // Also check for minimal data from history or cache
        if (drama == null) {
            val bookId = intent.getStringExtra("bookId")
            if (bookId != null) {
                // Try to get from cache first via Repository
                drama = com.ins.insdrama.api.DramaRepository.getDrama(bookId)
                if (drama == null) {
                    // Fetch full drama data from API
                    fetchDramaData(bookId)
                    return
                }
            }
        }

        if (drama == null) {
            Toast.makeText(this, "Data drama tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        loadDramaData()
    }

    private fun fetchDramaData(bookId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dramas = com.ins.insdrama.api.DramaRepository.getDramas(forceRefresh = false)
                withContext(Dispatchers.Main) {
                    drama = dramas.find { it.bookId == bookId }
                    if (drama != null) {
                        setupViews()
                        loadDramaData()
                    } else {
                        Toast.makeText(this@DetailActivity, "Drama tidak ditemukan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshEpisodeList()
    }

    private fun setupViews() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.playAllButton.setOnClickListener {
            val playerIntent = Intent(this, PlayerActivity::class.java)
            playerIntent.putExtra(PlayerActivity.EXTRA_DRAMA, drama)
            startActivity(playerIntent)
        }

        val d = drama ?: return
        episodeAdapter = EpisodeAdapter(
            dramaId = d.bookId,
            dramaTitle = d.title,
            onPlayClick = { episode ->
                val playerIntent = Intent(this, PlayerActivity::class.java)
                playerIntent.putExtra(PlayerActivity.EXTRA_DRAMA, drama)
                playerIntent.putExtra(PlayerActivity.EXTRA_EPISODE_INDEX, episode.index - 1)
                startActivity(playerIntent)
            },
            onDownloadClick = { episode ->
                handleDownloadClick(d, episode)
            }
        )

        binding.episodeRecyclerView.apply {
            adapter = episodeAdapter
            layoutManager = LinearLayoutManager(this@DetailActivity)
            isNestedScrollingEnabled = false
        }
    }

    private fun handleDownloadClick(d: Drama, episode: Episode) {
        if (DownloadManager.isEpisodeDownloaded(this, d.bookId, episode)) {
            if (DownloadManager.deleteEpisode(this, d.bookId, episode)) {
                Toast.makeText(this, "Download dihapus", Toast.LENGTH_SHORT).show()
                refreshEpisodeList()
            }
        } else {
            // Fix: Pass all necessary metadata to DownloadManager
            DownloadManager.downloadEpisode(this, d.bookId, d.title, episode, d.coverUrl, d.description)

            // Start the actual service to perform the download
            val serviceIntent = Intent(this, com.ins.insdrama.service.DownloadService::class.java).apply {
                putExtra(com.ins.insdrama.service.DownloadService.EXTRA_URL, episode.videoUrl)
                putExtra(com.ins.insdrama.service.DownloadService.EXTRA_FILENAME, "insdrama_${d.bookId}_ep${episode.index}.mp4")
                putExtra(com.ins.insdrama.service.DownloadService.EXTRA_EPISODE, "${d.title} - Episode ${episode.index}")
                // Add more metadata if needed for DownloadService to call markDownloadComplete
                putExtra("bookId", d.bookId)
                putExtra("dramaTitle", d.title)
                putExtra("episodeIndex", episode.index)
                putExtra("coverUrl", d.coverUrl)
                putExtra("synopsis", d.description)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            Toast.makeText(this, "Download dimulai: Episode ${episode.index}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshEpisodeList() {
        val d = drama ?: return
        episodeAdapter.submitList(d.episodes.toList())
    }

    private fun loadDramaData() {
        val d = drama ?: return

        Glide.with(this)
            .load(d.coverUrl)
            .centerCrop()
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder_cover)
            .into(binding.coverImage)

        binding.titleText.text = d.title
        binding.episodeCountText.text = "${d.episodes.size} Episode"

        val dateStr = try {
            if (d.createdAt.length >= 10) d.createdAt.substring(0, 10) else d.createdAt
        } catch (e: Exception) {
            d.createdAt
        }
        binding.dateText.text = dateStr

        binding.genreChipGroup.removeAllViews()
        d.genres.forEach { genre ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = genre
                isClickable = false
                isCheckable = false
                chipBackgroundColor = getColorStateList(R.color.chip_background)
                setTextColor(getColorStateList(R.color.chip_text))
            }
            binding.genreChipGroup.addView(chip)
        }

        binding.descriptionText.text = d.description
        episodeAdapter.submitList(d.episodes.toList())
    }
}
