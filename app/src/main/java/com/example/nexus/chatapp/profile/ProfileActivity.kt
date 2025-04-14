package com.example.nexus.chatapp.profile

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nexus.chatapp.R
import com.example.nexus.chatapp.auth.LoginActivity
import com.example.nexus.chatapp.databinding.ActivityProfileBinding
import com.example.nexus.chatapp.model.User
import com.example.nexus.chatapp.utils.LanguageManager
import com.example.nexus.chatapp.utils.LocalStorage
import com.example.nexus.chatapp.utils.ThemeManager
import com.example.nexus.chatapp.utils.hideKeyboard
import com.example.nexus.chatapp.utils.showToast
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var localStorage: LocalStorage
    private lateinit var themeManager: ThemeManager
    private lateinit var languageManager: LanguageManager
    
    private lateinit var currentUser: User
    private var isEditing = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize managers
        themeManager = ThemeManager(this)
        languageManager = LanguageManager(this)
        localStorage = LocalStorage(this)
        
        // Apply theme and language
        themeManager.applyThemeForActivity(this)
        languageManager.applyLanguageForActivity(this)
        
        // Use view binding
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.profile)
        
        // Get current user ID
        val currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("current_user_id", "") ?: ""
        
        if (currentUserId.isEmpty()) {
            navigateToLogin()
            return
        }
        
        loadUser(currentUserId)
        setupListeners()
    }
    
    private fun loadUser(userId: String) {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            val user = localStorage.getUserById(userId)
            
            binding.progressBar.visibility = View.GONE
            
            if (user == null) {
                showToast(getString(R.string.user_not_found))
                navigateToLogin()
                return@launch
            }
            
            currentUser = user
            displayUserInfo()
        }
    }
    
    private fun displayUserInfo() {
        binding.textUsername.text = currentUser.username
        binding.textFullName.text = currentUser.fullName
        binding.textEmail.text = currentUser.email
        
        // Set initial letter of the user's name as the avatar
        val initial = currentUser.fullName.firstOrNull()?.toString() ?: "?"
        binding.textAvatar.text = initial
        
        binding.viewInfo.visibility = View.VISIBLE
        binding.editInfo.visibility = View.GONE
        isEditing = false
    }
    
    private fun setupListeners() {
        // Edit profile button
        binding.buttonEdit.setOnClickListener {
            if (!isEditing) {
                startEditMode()
            } else {
                saveChanges()
            }
        }
        
        // Cancel edit button
        binding.buttonCancel.setOnClickListener {
            if (isEditing) {
                cancelEditMode()
            }
        }
        
        // Logout button
        binding.buttonLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun startEditMode() {
        isEditing = true
        
        // Set current values to edit fields
        binding.editFullName.setText(currentUser.fullName)
        binding.editEmail.setText(currentUser.email)
        
        // Show edit layout, hide view layout
        binding.viewInfo.visibility = View.GONE
        binding.editInfo.visibility = View.VISIBLE
        
        // Change button text
        binding.buttonEdit.setText(R.string.save)
        binding.buttonCancel.visibility = View.VISIBLE
    }
    
    private fun cancelEditMode() {
        isEditing = false
        
        // Show view layout, hide edit layout
        binding.viewInfo.visibility = View.VISIBLE
        binding.editInfo.visibility = View.GONE
        
        // Reset button text
        binding.buttonEdit.setText(R.string.edit)
        binding.buttonCancel.visibility = View.GONE
        
        hideKeyboard()
    }
    
    private fun saveChanges() {
        val fullName = binding.editFullName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        
        // Validate input
        if (fullName.isEmpty() || email.isEmpty()) {
            showToast(getString(R.string.all_fields_required))
            return
        }
        
        // Hide keyboard
        hideKeyboard()
        
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            // Update user
            val updatedUser = currentUser.copy(
                fullName = fullName,
                email = email
            )
            
            localStorage.updateUser(updatedUser)
            currentUser = updatedUser
            
            binding.progressBar.visibility = View.GONE
            
            // Update display
            displayUserInfo()
            showToast(getString(R.string.profile_updated))
        }
    }
    
    private fun logout() {
        // Clear current user from preferences
        getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply()
        
        navigateToLogin()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
