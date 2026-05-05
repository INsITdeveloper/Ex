package com.ins.insdrama.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ins.insdrama.R
import com.ins.insdrama.model.Drama

class DramaAdapter(
    private val context: android.content.Context,
    private var dramas: List<Drama>,
    private val onItemClick: (Drama) -> Unit
) : RecyclerView.Adapter<DramaAdapter.DramaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DramaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drama, parent, false)
        return DramaViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: DramaViewHolder, position: Int) {
        holder.bind(dramas[position])
    }

    override fun getItemCount() = dramas.size

    fun updateDramas(newDramas: List<Drama>) {
        dramas = newDramas
        notifyDataSetChanged()
    }

    class DramaViewHolder(
        itemView: View,
        private val onItemClick: (Drama) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val coverImage: ImageView = itemView.findViewById(R.id.coverImage)
        private val badgeText: TextView = itemView.findViewById(R.id.badgeText)
        private val viewCountText: TextView = itemView.findViewById(R.id.viewCountText)
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val subtitleText: TextView = itemView.findViewById(R.id.subtitleText)

        fun bind(drama: Drama) {
            titleText.text = drama.title
            subtitleText.text = "${drama.episodes.size} Episode"

            // Show badge for new dramas (first 5)
            badgeText.visibility = if (adapterPosition < 5) View.VISIBLE else View.GONE
            badgeText.text = "Baru"

            // Show view count
            viewCountText.text = "${(Math.random() * 10).toInt()}.${(Math.random() * 9).toInt()}M"

            Glide.with(itemView.context)
                .load(drama.coverUrl)
                .centerCrop()
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_cover)
                .into(coverImage)

            itemView.setOnClickListener {
                onItemClick(drama)
            }
        }
    }

    class DramaDiffCallback : DiffUtil.ItemCallback<Drama>() {
        override fun areItemsTheSame(oldItem: Drama, newItem: Drama): Boolean {
            return oldItem.bookId == newItem.bookId
        }

        override fun areContentsTheSame(oldItem: Drama, newItem: Drama): Boolean {
            return oldItem == newItem
        }
    }
}
