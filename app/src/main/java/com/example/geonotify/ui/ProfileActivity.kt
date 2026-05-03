package com.example.geonotify.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geonotify.R
import com.example.geonotify.data.GeofenceDatabase
import com.example.geonotify.data.GeofenceRepository
import com.example.geonotify.services.geofence.GeofenceHelper
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var viewModel: GeofenceViewModel
    private lateinit var adapter: GeofenceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Profile"

        // Initialize ViewModel
        val repository = GeofenceRepository(GeofenceDatabase.getDatabase(this).geofenceDao())
        val geofenceHelper = GeofenceHelper(this)
        viewModel = ViewModelProvider(this, GeofenceViewModelFactory(repository, geofenceHelper))[GeofenceViewModel::class.java]

        setupRecyclerView()
        observeGeofences()
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.geofenceRecyclerView)
        adapter = GeofenceAdapter { geofence ->
            viewModel.delete(geofence)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun observeGeofences() {
        val emptyState: LinearLayout = findViewById(R.id.emptyStateLayout)
        val recyclerView: RecyclerView = findViewById(R.id.geofenceRecyclerView)

        lifecycleScope.launch {
            viewModel.allGeofences.collect { geofences ->
                if (geofences.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.submitList(geofences)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}