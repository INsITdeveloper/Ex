package com.ins.insdrama

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.ins.insdrama.model.Drama

class PreviewFragment : Fragment() {

    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var dramas: List<Drama> = emptyList()
    private var currentIndex = 0

    private lateinit var titleText: TextView
    private lateinit var episodeInfoText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var genreChipGroup: ChipGroup
    private lateinit var watchNowButton: MaterialButton
    private lateinit var addToHistoryButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playerView = view.findViewById(R.id.previewPlayerView)
        titleText = view.findViewById(R.id.previewDramaTitle)
        episodeInfoText = view.findViewById(R.id.previewEpisodeInfo)
        descriptionText = view.findViewById(R.id.previewDescription)
        genreChipGroup = view.findViewById(R.id.previewGenreChipGroup)
        watchNowButton = view.findViewById(R.id.watchNowButton)
        addToHistoryButton = view.findViewById(R.id.addToHistoryButton)

        setupPlayer()
        loadData()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(requireContext()).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    // Handle player state
                }
            })
        }
        playerView?.player = player
    }

    private fun loadData() {
        (activity as? MainActivity)?.fetchDramas { list ->
            dramas = list
            if (dramas.isNotEmpty()) {
                updateUI(dramas[currentIndex])
            }
        }
    }

    private fun updateUI(drama: Drama) {
        titleText.text = drama.title
        episodeInfoText.text = "${drama.episodes.size} Episode"
        descriptionText.text = drama.description

        genreChipGroup.removeAllViews()
        drama.genres.take(3).forEach { genre ->
            val chip = Chip(requireContext()).apply {
                text = genre
                textSize = 10f
                isClickable = false
            }
            genreChipGroup.addView(chip)
        }

        if (drama.episodes.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(drama.episodes[0].videoUrl)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
        }

        watchNowButton.setOnClickListener {
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_DRAMA, drama)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
    }
}
