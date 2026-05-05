package com.ins.insdrama

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ins.insdrama.api.ApiClient
import com.ins.insdrama.model.Drama
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    // Store fragment instances to maintain state
    private val homeFragment = HomeFragment()
    private val historyFragment = HistoryFragment()
    private val downloadsFragment = DownloadsFragment()
    private val settingsFragment = SettingsFragment()

    // Cache dramas to avoid reloading on navigation
    private var cachedDramas: List<Drama>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Load initial fragment (Home/Temukan)
        if (savedInstanceState == null) {
            loadFragment(homeFragment)
            bottomNavigation.selectedItemId = R.id.nav_temukan
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_temukan -> {
                    loadFragment(homeFragment)
                    true
                }
                R.id.nav_cuplikan -> {
                    // For now, show home fragment (can add preview later)
                    loadFragment(homeFragment)
                    true
                }
                R.id.nav_history -> {
                    loadFragment(historyFragment)
                    true
                }
                R.id.nav_downloads -> {
                    loadFragment(downloadsFragment)
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commitAllowingStateLoss()
    }

    fun fetchDramas(forceRefresh: Boolean = false, onComplete: (List<Drama>) -> Unit) {
        if (!forceRefresh && cachedDramas != null) {
            onComplete(cachedDramas!!)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.dramaApi.getDramas()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val dramas = response.body() ?: emptyList()
                        cachedDramas = dramas
                        onComplete(dramas)
                    } else {
                        onComplete(cachedDramas ?: emptyList())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(cachedDramas ?: emptyList())
                }
            }
        }
    }

    fun getCachedDrama(bookId: String): Drama? {
        return cachedDramas?.find { it.bookId == bookId }
    }

    fun saveToHistory(drama: Drama, episodeIndex: Int, watchedAt: Long = System.currentTimeMillis()) {
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
        historyList.removeAll { it.bookId == drama.bookId }

        // Add new entry at the beginning
        historyList.add(0, HistoryItem(
            bookId = drama.bookId,
            title = drama.title,
            coverUrl = drama.coverUrl,
            episodeIndex = episodeIndex,
            watchedAt = watchedAt
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
}
