package com.example.geonotify.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.geonotify.R
import com.example.geonotify.maps.MapsActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "GeoNotify"

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Main Feature Buttons
        findViewById<MaterialButton>(R.id.btnCreateGeofence).setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnEditGeofence).setOnClickListener {
            Toast.makeText(this, "Edit Geofence coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnDeleteGeofence).setOnClickListener {
            Toast.makeText(this, "Delete Geofence coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                Toast.makeText(this, "Profile Selected", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_geofence -> {
                // Already on main/geofence screen
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_disable -> {
                Toast.makeText(this, "Disable Features Selected", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}