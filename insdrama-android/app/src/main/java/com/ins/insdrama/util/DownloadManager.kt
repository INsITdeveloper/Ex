package com.ins.insdrama.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.ins.insdrama.model.Drama
import com.ins.insdrama.model.Episode
import java.io.File

object DownloadManager {

    private const val PREFS_NAME = "insdrama_downloads"
    private const val DOWNLOADS_DIR = "downloads"

    data class DownloadInfo(
        val bookId: String,
        val dramaTitle: String,
        val episodeIndex: Int,
        val filePath: String,
        val coverUrl: String,
        val synopsis: String
    )

    fun getDownloadPath(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), DOWNLOADS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getLocalFilePath(context: Context, bookId: String, episode: Episode): String? {
        val downloadDir = getDownloadPath(context)
        val fileName = "insdrama_${bookId}_ep${episode.index}.mp4"
        val file = File(downloadDir, fileName)
        return if (file.exists()) file.absolutePath else null
    }

    fun isEpisodeDownloaded(context: Context, bookId: String, episode: Episode): Boolean {
        return getLocalFilePath(context, bookId, episode) != null
    }

    fun downloadEpisode(context: Context, bookId: String, dramaTitle: String, episode: Episode, coverUrl: String = "", synopsis: String = "") {
        // This would be called from DownloadService
        // For now, just mark as downloading
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        val downloads = getDownloadsList(prefs)

        val downloadKey = "${bookId}_ep${episode.index}"
        val existing = downloads.toMutableList()

        // Check if already exists
        val exists = existing.any { it.startsWith(downloadKey) }
        if (!exists) {
            existing.add(downloadKey)
            prefs.edit().putStringSet("downloading", existing.toSet()).apply()
        }
    }

    fun markDownloadComplete(context: Context, bookId: String, dramaTitle: String, episodeIndex: Int, coverUrl: String, synopsis: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        val downloads = getDownloadsList(prefs)
        val downloadKey = "${bookId}_ep${episodeIndex}"

        // Remove from downloading
        val downloading = prefs.getStringSet("downloading", emptySet())?.toMutableList() ?: mutableListOf()
        downloading.remove(downloadKey)
        prefs.edit().putStringSet("downloading", downloading.toSet()).apply()

        // Add to completed downloads
        if (!downloads.contains(downloadKey)) {
            val newDownloads = downloads.toMutableList()
            newDownloads.add(downloadKey)
            prefs.edit().putStringSet("downloads", newDownloads.toSet()).apply()

            // Save metadata
            val metadata = prefs.getString("download_metadata", "{}") ?: "{}"
            val metadataJson = org.json.JSONObject(metadata)
            val episodeJson = org.json.JSONObject()
            episodeJson.put("bookId", bookId)
            episodeJson.put("dramaTitle", dramaTitle)
            episodeJson.put("episodeIndex", episodeIndex)
            episodeJson.put("coverUrl", coverUrl)
            episodeJson.put("synopsis", synopsis)
            metadataJson.put(downloadKey, episodeJson)
            prefs.edit().putString("download_metadata", metadataJson.toString()).apply()
        }
    }

    fun deleteEpisode(context: Context, bookId: String, episode: Episode): Boolean {
        val downloadDir = getDownloadPath(context)
        val fileName = "insdrama_${bookId}_ep${episode.index}.mp4"
        val file = File(downloadDir, fileName)

        if (file.exists()) {
            file.delete()

            // Remove from downloads list
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val downloads = getDownloadsList(prefs)
            val downloadKey = "${bookId}_ep${episode.index}"
            val newDownloads = downloads.filter { it != downloadKey }
            prefs.edit().putStringSet("downloads", newDownloads.toSet()).apply()

            // Remove metadata
            val metadata = prefs.getString("download_metadata", "{}") ?: "{}"
            val metadataJson = org.json.JSONObject(metadata)
            metadataJson.remove(downloadKey)
            prefs.edit().putString("download_metadata", metadataJson.toString()).apply()

            return true
        }
        return false
    }

    fun getAllDownloads(context: Context): List<DownloadInfo> {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        val downloads = getDownloadsList(prefs)
        val metadata = prefs.getString("download_metadata", "{}") ?: "{}"
        val metadataJson = org.json.JSONObject(metadata)

        return downloads.mapNotNull { downloadKey ->
            try {
                if (metadataJson.has(downloadKey)) {
                    val episodeJson = metadataJson.getJSONObject(downloadKey)
                    DownloadInfo(
                        bookId = episodeJson.getString("bookId"),
                        dramaTitle = episodeJson.getString("dramaTitle"),
                        episodeIndex = episodeJson.getInt("episodeIndex"),
                        filePath = getFilePathFromKey(context, downloadKey),
                        coverUrl = episodeJson.getString("coverUrl"),
                        synopsis = episodeJson.optString("synopsis", "")
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getFilePathFromKey(context: Context, downloadKey: String): String {
        val downloadDir = getDownloadPath(context)
        val fileName = "${downloadKey}.mp4"
        val file = File(downloadDir, fileName)
        return file.absolutePath
    }

    private fun getDownloadsList(prefs: android.content.SharedPreferences): Set<String> {
        return prefs.getStringSet("downloads", emptySet()) ?: emptySet()
    }
}
