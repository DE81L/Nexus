package com.example.nexus.chatapp.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nexus.chatapp.R
import com.example.nexus.chatapp.databinding.ActivitySettingsBinding
import com.example.nexus.chatapp.model.User
import com.example.nexus.chatapp.utils.LanguageManager
import com.example.nexus.chatapp.utils.LocalStorage
import com.example.nexus.chatapp.utils.ThemeManager
import com.example.nexus.chatapp.utils.showToast
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var localStorage: LocalStorage
    private lateinit var themeManager: ThemeManager
    private lateinit var languageManager: LanguageManager
    
    private lateinit var currentUser: User
    private var currentUserId: String = ""
    
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
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        
        // Get current user ID
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("current_user_id", "") ?: ""
        
        if (currentUserId.isEmpty()) {
            finish()
            return
        }
        
        loadCurrentUser()
        setupThemeSwitcher()
        setupLanguageSwitcher()
    }
    
    private fun loadCurrentUser() {
        lifecycleScope.launch {
            val user = localStorage.getUserById(currentUserId)
            if (user != null) {
                currentUser = user
                updateUI()
            } else {
                finish()
            }
        }
    }
    
    private fun updateUI() {
        // Set theme switch based on user preference
        binding.switchDarkMode.isChecked = currentUser.darkModeEnabled
        
        // Set language based on user preference
        binding.switchLanguage.isChecked = currentUser.language == LanguageManager.LANGUAGE_RUSSIAN
    }
    
    private fun setupThemeSwitcher() {
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Toggle theme
            themeManager.setDarkMode(isChecked)
            
            // Update user preference
            lifecycleScope.launch {
                val updatedUser = currentUser.copy(darkModeEnabled = isChecked)
                localStorage.updateUser(updatedUser)
                currentUser = updatedUser
            }
            
            // Recreate activity to apply theme
            recreate()
        }
    }
    
    private fun setupLanguageSwitcher() {
        binding.switchLanguage.setOnCheckedChangeListener { _, isChecked ->
            // Set language (en or ru)
            val newLanguage = if (isChecked) LanguageManager.LANGUAGE_RUSSIAN else LanguageManager.LANGUAGE_ENGLISH
            languageManager.setLanguage(newLanguage)
            
            // Update user preference
            lifecycleScope.launch {
                val updatedUser = currentUser.copy(language = newLanguage)
                localStorage.updateUser(updatedUser)
                currentUser = updatedUser
            }
            
            // Show message about language change
            showToast(getString(R.string.language_changed))
            
            // Recreate activity to apply language
            recreate()
        }
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
