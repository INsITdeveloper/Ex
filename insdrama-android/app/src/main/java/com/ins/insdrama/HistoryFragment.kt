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

class HistoryFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var historyAdapter: HistoryAdapter? = null
    private var historyList: List<MainActivity.HistoryItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.historyRecyclerView)
        setupRecyclerView()
        loadHistory()
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun setupRecyclerView() {
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = HistoryAdapter { historyItem ->
            // Open the drama detail
            val intent = Intent(requireContext(), DetailActivity::class.java)
            // We need to fetch full drama data, for now just pass minimal info
            intent.putExtra("bookId", historyItem.bookId)
            intent.putExtra("title", historyItem.title)
            startActivity(intent)
        }
        recyclerView?.adapter = historyAdapter
    }

    private fun loadHistory() {
        val prefs = requireActivity().getSharedPreferences("insdrama_history", 0)
        val historyJson = prefs.getString("history", "[]") ?: "[]"

        val list = try {
            val jsonArray = org.json.JSONArray(historyJson)
            val historyList = mutableListOf<MainActivity.HistoryItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                historyList.add(MainActivity.HistoryItem(
                    bookId = obj.getString("bookId"),
                    title = obj.getString("title"),
                    coverUrl = obj.getString("coverUrl"),
                    episodeIndex = obj.getInt("episodeIndex"),
                    watchedAt = obj.getLong("watchedAt")
                ))
            }
            historyList
        } catch (e: Exception) {
            emptyList()
        }

        historyList = list
        historyAdapter?.updateHistory(list)
    }

    class HistoryAdapter(
        private val onItemClick: (MainActivity.HistoryItem) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

        private var historyList: List<MainActivity.HistoryItem> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history, parent, false)
            return HistoryViewHolder(view, onItemClick)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            holder.bind(historyList[position])
        }

        override fun getItemCount() = historyList.size

        fun updateHistory(newHistory: List<MainActivity.HistoryItem>) {
            historyList = newHistory
            notifyDataSetChanged()
        }

        class HistoryViewHolder(
            itemView: View,
            private val onItemClick: (MainActivity.HistoryItem) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {

            private val coverImage: ImageView = itemView.findViewById(R.id.coverImage)
            private val titleText: TextView = itemView.findViewById(R.id.titleText)
            private val episodeText: TextView = itemView.findViewById(R.id.episodeText)
            private val watchedAtText: TextView = itemView.findViewById(R.id.watchedAtText)

            fun bind(history: MainActivity.HistoryItem) {
                titleText.text = history.title
                episodeText.text = "Episode ${history.episodeIndex + 1}"

                // Format watched time
                val timeAgo = formatTimeAgo(history.watchedAt)
                watchedAtText.text = timeAgo

                Glide.with(itemView.context)
                    .load(history.coverUrl)
                    .centerCrop()
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.placeholder_cover)
                    .into(coverImage)

                itemView.setOnClickListener {
                    onItemClick(history)
                }
            }

            private fun formatTimeAgo(timestamp: Long): String {
                val now = System.currentTimeMillis()
                val diff = now - timestamp

                return when {
                    diff < 60000 -> "Baru saja"
                    diff < 3600000 -> "${diff / 60000} menit yang lalu"
                    diff < 86400000 -> "${diff / 3600000} jam yang lalu"
                    diff < 604800000 -> "${diff / 86400000} hari yang lalu"
                    else -> "${diff / 604800000} minggu yang lalu"
                }
            }
        }
    }
}
