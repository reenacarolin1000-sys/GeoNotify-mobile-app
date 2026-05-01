package com.example.geonotify.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.geonotify.R
import com.example.geonotify.authentication.AuthenticationActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Show splash for 1500ms (Adjusted for better UX, 2ms is too fast for human perception)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
            finish()
        }, 1500)
    }
}