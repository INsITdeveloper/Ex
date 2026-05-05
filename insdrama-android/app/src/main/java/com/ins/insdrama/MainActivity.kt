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
    private val previewFragment = PreviewFragment()
    private val historyFragment = HistoryFragment()
    private val downloadsFragment = DownloadsFragment()
    private val settingsFragment = SettingsFragment()

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
                    loadFragment(previewFragment)
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
        val transaction = supportFragmentManager.beginTransaction()

        // Hide all fragments first
        val fragments = listOf(homeFragment, previewFragment, historyFragment, downloadsFragment, settingsFragment)
        fragments.forEach { if (it.isAdded) transaction.hide(it) }

        // Show or add the target fragment
        if (fragment.isAdded) {
            transaction.show(fragment)
        } else {
            transaction.add(R.id.fragmentContainer, fragment)
        }

        transaction.commitAllowingStateLoss()
    }

    fun fetchDramas(forceRefresh: Boolean = false, onComplete: (List<Drama>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val dramas = com.ins.insdrama.api.DramaRepository.getDramas(forceRefresh)
            withContext(Dispatchers.Main) {
                onComplete(dramas)
            }
        }
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
