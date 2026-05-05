package com.ins.insdrama.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Drama(
    @SerialName("book_id")
    val bookId: String,
    val title: String,
    val genres: List<String>,
    val description: String,
    @SerialName("cover_url")
    val coverUrl: String,
    @SerialName("created_at")
    val createdAt: String,
    val episodes: List<Episode>
) : Parcelable

@Parcelize
@Serializable
data class Episode(
    val index: Int,
    @SerialName("video_url")
    val videoUrl: String
) : Parcelable
