package com.example.geonotify.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.geonotify.R
import com.example.geonotify.permissions.PermissionsActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var phoneEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginBtn: MaterialButton
    private lateinit var registerBtn: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        mAuth = FirebaseAuth.getInstance()

        phoneEditText = findViewById(R.id.phoneEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginBtn = findViewById(R.id.loginBtn)
        registerBtn = findViewById(R.id.registerBtn)

        loginBtn.setOnClickListener {
            val phoneInput = phoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (phoneInput.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = generateEmail(phoneInput)
            Log.d("AUTH_DEBUG", "Logging in with: $email")

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful 🎉", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, PermissionsActivity::class.java))
                        finish()
                    } else {
                        Log.e("AUTH_DEBUG", "Login Failed", task.exception)
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        registerBtn.setOnClickListener {
            val phoneInput = phoneEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (phoneInput.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields to register", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = generateEmail(phoneInput)
            Log.d("AUTH_DEBUG", "Registering with: $email")

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration Successful 🎉", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, PermissionsActivity::class.java))
                        finish()
                    } else {
                        Log.e("AUTH_DEBUG", "Registration Failed", task.exception)
                        Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun generateEmail(phone: String): String {
        // Remove all non-numeric characters to ensure valid email prefix
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        return "user_$cleanPhone@geonotify.com"
    }
}