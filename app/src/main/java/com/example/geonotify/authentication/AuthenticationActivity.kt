package com.example.geonotify.authentication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.geonotify.R
import com.example.geonotify.permissions.PermissionsActivity

class AuthenticationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        val btn = findViewById<Button>(R.id.loginBtn)

        btn.setOnClickListener {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }
    }
}