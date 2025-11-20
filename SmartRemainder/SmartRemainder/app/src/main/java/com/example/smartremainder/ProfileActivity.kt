package com.example.smartremainder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.smartremainder.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var dbHelper: UserDatabaseHelper
    private lateinit var mainDbHelper: SignupSQL // Permanent database
    private var currentUser: UserProfile? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                // Request persistent permissions for the selected image
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(imageUri, takeFlags)

                currentUser?.let {
                    // Save to the session database
                    val updatedUser = it.copy(profileImage = imageUri.toString())
                    dbHelper.insertOrUpdateUser(updatedUser)

                    // Save to the permanent database
                    mainDbHelper.updateUserProfileImage(it.email, imageUri.toString())

                    loadUserProfile()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = UserDatabaseHelper(this)
        mainDbHelper = SignupSQL(this)
        setupToolbar()
        loadUserProfile()
        setupButtons()

        binding.ivEditProfileImage.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        // Use ACTION_OPEN_DOCUMENT to get a persistable URI
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadUserProfile() {
        currentUser = dbHelper.getUserProfile()
        currentUser?.let {
            displayUserProfile(it)
        } ?: run {
            Toast.makeText(this, "User profile not found.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun displayUserProfile(user: UserProfile) {
        with(binding) {
            tvUserName.text = user.name
            tvUsername.text = "@${user.username}"
            tvEmail.text = user.email
            tvPhone.text = "Phone: ${user.phone}"

            if (!user.profileImage.isNullOrEmpty()) {
                Glide.with(this@ProfileActivity)
                    .load(Uri.parse(user.profileImage))
                    .circleCrop()
                    .into(ivProfilePicture)
            } else {
                // Set a default image if no profile picture is selected
                Glide.with(this@ProfileActivity)
                    .load(R.drawable.ic_user)
                    .circleCrop()
                    .into(ivProfilePicture)
            }
        }
    }

    private fun setupButtons() {
        binding.btnEditProfile.setOnClickListener {
            openEditProfileDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun openEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)

        currentUser?.let { user ->
            etName.setText(user.name)
            etUsername.setText(user.username)
            etEmail.setText(user.email)
            etPhone.setText(user.phone)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedUser = UserProfile(
                    id = currentUser?.id ?: 0,
                    name = etName.text.toString(),
                    username = etUsername.text.toString(),
                    email = etEmail.text.toString(),
                    phone = etPhone.text.toString(),
                    profileImage = currentUser?.profileImage
                )
                dbHelper.insertOrUpdateUser(updatedUser)
                loadUserProfile()
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun performLogout() {
        dbHelper.deleteUser()

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, WelComePage::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}