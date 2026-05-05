package com.ins.insdrama.api

import com.ins.insdrama.model.Drama
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DramaRepository {
    private var cachedDramas: List<Drama>? = null

    suspend fun getDramas(forceRefresh: Boolean = false): List<Drama> {
        if (!forceRefresh && cachedDramas != null) {
            return cachedDramas!!
        }

        return try {
            val response = ApiClient.dramaApi.getDramas()
            if (response.isSuccessful) {
                cachedDramas = response.body() ?: emptyList()
                cachedDramas!!
            } else {
                cachedDramas ?: emptyList()
            }
        } catch (e: Exception) {
            cachedDramas ?: emptyList()
        }
    }

    fun getDrama(bookId: String): Drama? {
        return cachedDramas?.find { it.bookId == bookId }
    }
}
