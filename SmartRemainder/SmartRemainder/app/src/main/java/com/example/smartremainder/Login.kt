package com.example.smartremainder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Login : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView

    private lateinit var dbHelper: SignupSQL
    private lateinit var userDbHelper: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)

        // Initialize database helpers
        dbHelper = SignupSQL(this)
        userDbHelper = UserDatabaseHelper(this)

        // Login button click
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show()
            } else {
                val isValid = dbHelper.checkLogin(username, password)
                if (isValid) {
                    // Fetch user details from SignupSQL database
                    val user = dbHelper.getUserByEmail(username)

                    if (user != null) {
                        // Save user details to UserProfile database
                        val userProfile = UserProfile(
                            name = user.name,
                            username = user.name.replace(" ", "_").toLowerCase(),
                            email = user.email,
                            phone = user.phone,
                            profileImage = user.profileImage
                        )
                        userDbHelper.insertOrUpdateUser(userProfile)

                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Could not retrieve user details.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Navigate to Sign Up page
        tvSignUp.setOnClickListener {
            val intent = Intent(this, Signukt::class.java)
            startActivity(intent)
        }
    }
}