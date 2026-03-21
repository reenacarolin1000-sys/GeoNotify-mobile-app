package com.example.geonotify.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.geonotify.R
import com.example.geonotify.maps.MapsActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCreate = findViewById<MaterialButton>(R.id.btnCreateGeofence)
        val btnEdit = findViewById<MaterialButton>(R.id.btnEditGeofence)
        val btnDelete = findViewById<MaterialButton>(R.id.btnDeleteGeofence)

        btnCreate.setOnClickListener {
            // Placeholder: Navigating to Map for creation
            startActivity(Intent(this, MapsActivity::class.java))
        }

        btnEdit.setOnClickListener {
            Toast.makeText(this, "Edit Geofence coming soon", Toast.LENGTH_SHORT).show()
        }

        btnDelete.setOnClickListener {
            Toast.makeText(this, "Delete Geofence coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}