package com.ins.insdrama

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.ins.insdrama.adapter.DramaAdapter
import com.ins.insdrama.model.Drama

class HomeFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    private var progressBar: ProgressBar? = null
    private var searchEditText: EditText? = null
    private var searchButton: TextView? = null
    private var dramaAdapter: DramaAdapter? = null
    private var allDramas: List<Drama> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        progressBar = view.findViewById(R.id.progressBar)
        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)

        setupRecyclerView()
        setupSearch()
        setupCategoryChips(view)
        loadDramas(forceRefresh = false)

        swipeRefresh?.setOnRefreshListener {
            loadDramas(forceRefresh = true)
        }
    }

    private fun setupRecyclerView() {
        recyclerView?.layoutManager = GridLayoutManager(requireContext(), 2)
        dramaAdapter = DramaAdapter(requireContext(), emptyList()) { drama ->
            // Open detail with correct extra key
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_DRAMA, drama)
            startActivity(intent)
        }
        recyclerView?.adapter = dramaAdapter
    }

    private fun setupSearch() {
        searchButton?.setOnClickListener {
            val query = searchEditText?.text.toString()
            if (query.isNotBlank()) {
                filterDramas(query)
            }
        }
    }

    private fun setupCategoryChips(view: View) {
        val chipPopuler = view.findViewById<Chip>(R.id.chipPopuler)
        val chipAnime = view.findViewById<Chip>(R.id.chipAnime)
        val chipSistem = view.findViewById<Chip>(R.id.chipSistem)
        val chipKeluarga = view.findViewById<Chip>(R.id.chipKeluarga)

        // Set Populer as selected by default
        chipPopuler.isChecked = true

        chipPopuler.setOnClickListener {
            chipPopuler.isChecked = true
            chipAnime.isChecked = false
            chipSistem.isChecked = false
            chipKeluarga.isChecked = false
            loadDramas(forceRefresh = false)
        }
        chipAnime.setOnClickListener {
            chipPopuler.isChecked = false
            chipAnime.isChecked = true
            chipSistem.isChecked = false
            chipKeluarga.isChecked = false
            // Filter by anime
        }
        chipSistem.setOnClickListener {
            chipPopuler.isChecked = false
            chipAnime.isChecked = false
            chipSistem.isChecked = true
            chipKeluarga.isChecked = false
            // Filter by sistem
        }
        chipKeluarga.setOnClickListener {
            chipPopuler.isChecked = false
            chipAnime.isChecked = false
            chipSistem.isChecked = false
            chipKeluarga.isChecked = true
            // Filter by keluarga
        }
    }

    private fun loadDramas(forceRefresh: Boolean) {
        swipeRefresh?.isRefreshing = true
        (activity as? MainActivity)?.fetchDramas(forceRefresh) { dramas ->
            swipeRefresh?.isRefreshing = false
            allDramas = dramas
            dramaAdapter?.updateDramas(dramas)
        }
    }

    private fun filterDramas(query: String) {
        val filtered = allDramas.filter {
            it.title.contains(query, ignoreCase = true)
        }
        dramaAdapter?.updateDramas(filtered)
    }
}
