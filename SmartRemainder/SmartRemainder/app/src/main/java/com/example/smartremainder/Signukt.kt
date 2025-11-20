package com.example.smartremainder

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class Signukt : AppCompatActivity() {

    private lateinit var dbHelper: SignupSQL
    private lateinit var userDbHelper: UserDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signukt)

        dbHelper = SignupSQL(this)
        userDbHelper = UserDatabaseHelper(this)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            val name = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Please agree to the Terms & Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.checkUserExists(email)) {
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isInserted = dbHelper.insertUser(name, email, phone, password)
            if (isInserted) {
                // Also save to the UserProfile database
                val userProfile = UserProfile(name = name, username = name.replace(" ", "_").toLowerCase(), email = email, phone = phone)
                userDbHelper.insertOrUpdateUser(userProfile)

                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                
                // Navigate to the main activity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show()
            }
        }
    }
}