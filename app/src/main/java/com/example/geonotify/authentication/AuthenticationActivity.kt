package com.example.geonotify.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.geonotify.R
import com.example.geonotify.ui.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var phoneEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var nameEditText: TextInputEditText
    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var authTitle: android.widget.TextView
    private lateinit var loginBtn: MaterialButton
    private lateinit var toggleModeBtn: MaterialButton
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        mAuth = FirebaseAuth.getInstance()

        authTitle = findViewById(R.id.authTitle)
        phoneEditText = findViewById(R.id.phoneEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.nameEditText)
        nameInputLayout = findViewById(R.id.nameInputLayout)
        loginBtn = findViewById(R.id.loginBtn)
        toggleModeBtn = findViewById(R.id.registerBtn)

        loginBtn.setOnClickListener {
            if (isLoginMode) {
                handleLogin()
            } else {
                handleRegistration()
            }
        }

        toggleModeBtn.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUI()
        }
    }

    private fun updateUI() {
        if (isLoginMode) {
            authTitle.text = "Welcome to GeoNotify"
            loginBtn.text = "Login"
            toggleModeBtn.text = "Don't have an account? Register"
            nameInputLayout.visibility = android.view.View.GONE
        } else {
            authTitle.text = "Create Account"
            loginBtn.text = "Register"
            toggleModeBtn.text = "Already have an account? Login"
            nameInputLayout.visibility = android.view.View.VISIBLE
        }
    }

    private fun handleLogin() {
        val phoneInput = phoneEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (phoneInput.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val email = generateEmail(phoneInput)
        Log.d("AUTH_DEBUG", "Logging in with: $email")

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful 🎉", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.e("AUTH_DEBUG", "Login Failed", task.exception)
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun handleRegistration() {
        val name = nameEditText.text.toString().trim()
        val phoneInput = phoneEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (name.isEmpty() || phoneInput.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields to register", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        val email = generateEmail(phoneInput)
        Log.d("AUTH_DEBUG", "Registering with: $email")

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = mAuth.currentUser?.uid
                    if (userId != null) {
                        saveUserToDatabase(userId, name, phoneInput)
                    }
                } else {
                    Log.e("AUTH_DEBUG", "Registration Failed", task.exception)
                    val rawError = task.exception?.message ?: "Unknown error"
                    val friendlyError = if (rawError.contains("email address is already in use", ignoreCase = true)) {
                        "This phone number is already registered. Please login instead."
                    } else if (rawError.contains("badly formatted", ignoreCase = true)) {
                        "Invalid phone format."
                    } else {
                        rawError
                    }
                    Toast.makeText(this, friendlyError, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToDatabase(userId: String, name: String, phone: String) {
        val userMap = mapOf(
            "uid" to userId,
            "name" to name,
            "phone" to phone,
            "createdAt" to System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance().getReference("Users")
            .child(userId)
            .setValue(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration Successful 🎉", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("AUTH_DEBUG", "Database Error", e)
                Toast.makeText(this, "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateEmail(phone: String): String {
        // Remove all non-numeric characters to ensure valid email prefix
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        return "user_$cleanPhone@geonotify.com"
    }
}