package com.ins.insdrama.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ins.insdrama.R
import com.ins.insdrama.model.Episode
import com.ins.insdrama.util.DownloadManager

class EpisodeAdapter(
    private val dramaId: String,
    private val dramaTitle: String,
    private val onPlayClick: (Episode) -> Unit,
    private val onDownloadClick: (Episode) -> Unit
) : ListAdapter<Episode, EpisodeAdapter.EpisodeViewHolder>(EpisodeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = getItem(position)
        holder.bind(episode, dramaId, onPlayClick, onDownloadClick)
    }

    class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val episodeNumberText: TextView = itemView.findViewById(R.id.episodeNumberText)
        private val episodeTitleText: TextView = itemView.findViewById(R.id.episodeTitleText)
        private val episodeStatusText: TextView = itemView.findViewById(R.id.episodeStatusText)
        private val downloadedIndicator: ImageView = itemView.findViewById(R.id.downloadedIndicator)
        private val downloadButton: ImageButton = itemView.findViewById(R.id.downloadButton)
        private val playIcon: ImageView = itemView.findViewById(R.id.playIcon)
        private val downloadProgress: ProgressBar = itemView.findViewById(R.id.downloadProgress)

        fun bind(
            episode: Episode,
            dramaId: String,
            onPlayClick: (Episode) -> Unit,
            onDownloadClick: (Episode) -> Unit
        ) {
            episodeNumberText.text = episode.index.toString()
            episodeTitleText.text = "Episode ${episode.index}"

            val isDownloaded = DownloadManager.isEpisodeDownloaded(itemView.context, dramaId, episode)

            if (isDownloaded) {
                downloadedIndicator.visibility = View.VISIBLE
                episodeStatusText.text = "Downloaded - Siap offline"
                episodeStatusText.setTextColor(itemView.context.getColor(R.color.primary))
                downloadButton.setImageResource(R.drawable.ic_delete)
                downloadButton.contentDescription = "Hapus download"
                downloadProgress.visibility = View.GONE
            } else {
                downloadedIndicator.visibility = View.GONE
                episodeStatusText.text = "Tap untuk download"
                episodeStatusText.setTextColor(itemView.context.getColor(R.color.text_secondary))
                downloadButton.setImageResource(R.drawable.ic_download)
                downloadButton.contentDescription = "Download"
                downloadProgress.visibility = View.GONE
            }

            playIcon.setOnClickListener {
                onPlayClick(episode)
            }

            downloadButton.setOnClickListener {
                onDownloadClick(episode)
            }

            itemView.setOnClickListener {
                onPlayClick(episode)
            }
        }
    }

    class EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.index == newItem.index
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }
    }
}
