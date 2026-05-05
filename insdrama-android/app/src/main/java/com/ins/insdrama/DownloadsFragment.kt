package com.ins.insdrama

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ins.insdrama.R
import com.ins.insdrama.model.Drama
import com.ins.insdrama.model.Episode
import com.ins.insdrama.util.DownloadManager
import java.io.File

class DownloadsFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var downloadsAdapter: DownloadsAdapter? = null
    private var downloadList: List<DownloadItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.downloadsRecyclerView)
        setupRecyclerView()
        loadDownloads()
    }

    override fun onResume() {
        super.onResume()
        loadDownloads()
    }

    private fun setupRecyclerView() {
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        downloadsAdapter = DownloadsAdapter { downloadItem ->
            // Play the downloaded episode offline
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            // Create minimal drama object with local file
            val episode = Episode(
                index = downloadItem.episodeIndex,
                videoUrl = downloadItem.filePath
            )
            val drama = Drama(
                bookId = downloadItem.bookId,
                title = downloadItem.dramaTitle,
                genres = emptyList(),
                description = downloadItem.synopsis,
                coverUrl = downloadItem.coverUrl,
                createdAt = "",
                episodes = listOf(episode)
            )
            intent.putExtra(PlayerActivity.EXTRA_DRAMA, drama)
            intent.putExtra(PlayerActivity.EXTRA_EPISODE_INDEX, 0)
            startActivity(intent)
        }
        recyclerView?.adapter = downloadsAdapter
    }

    private fun loadDownloads() {
        val downloads = DownloadManager.getAllDownloads(requireContext())
        downloadList = downloads.map { download ->
            DownloadItem(
                bookId = download.bookId,
                dramaTitle = download.dramaTitle,
                episodeIndex = download.episodeIndex,
                filePath = download.filePath,
                coverUrl = download.coverUrl,
                synopsis = download.synopsis,
                fileSize = getFileSize(download.filePath)
            )
        }
        downloadsAdapter?.updateDownloads(downloadList)
    }

    private fun getFileSize(filePath: String): String {
        return try {
            val file = File(filePath)
            val size = file.length()
            when {
                size < 1024 * 1024 -> "${size / 1024} KB"
                size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
                else -> "${size / (1024 * 1024 * 1024)} GB"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    data class DownloadItem(
        val bookId: String,
        val dramaTitle: String,
        val episodeIndex: Int,
        val filePath: String,
        val coverUrl: String,
        val synopsis: String,
        val fileSize: String
    )

    class DownloadsAdapter(
        private val onItemClick: (DownloadItem) -> Unit
    ) : RecyclerView.Adapter<DownloadsAdapter.DownloadsViewHolder>() {

        private var downloadList: List<DownloadItem> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_download, parent, false)
            return DownloadsViewHolder(view, onItemClick)
        }

        override fun onBindViewHolder(holder: DownloadsViewHolder, position: Int) {
            holder.bind(downloadList[position])
        }

        override fun getItemCount() = downloadList.size

        fun updateDownloads(newDownloads: List<DownloadItem>) {
            downloadList = newDownloads
            notifyDataSetChanged()
        }

        class DownloadsViewHolder(
            itemView: View,
            private val onItemClick: (DownloadItem) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {

            private val coverImage: ImageView = itemView.findViewById(R.id.coverImage)
            private val titleText: TextView = itemView.findViewById(R.id.titleText)
            private val episodeText: TextView = itemView.findViewById(R.id.episodeText)
            private val fileSizeText: TextView = itemView.findViewById(R.id.fileSizeText)
            private val synopsisText: TextView = itemView.findViewById(R.id.synopsisText)

            fun bind(download: DownloadItem) {
                titleText.text = download.dramaTitle
                episodeText.text = "Episode ${download.episodeIndex + 1}"
                fileSizeText.text = download.fileSize
                synopsisText.text = download.synopsis.take(100) + "..."

                Glide.with(itemView.context)
                    .load(download.coverUrl)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_cover)
                    .into(coverImage)

                itemView.setOnClickListener {
                    onItemClick(download)
                }
            }
        }
    }
}
