package com.example.nexus.chatapp.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nexus.chatapp.MainActivity
import com.example.nexus.chatapp.databinding.ActivityRegisterBinding
import com.example.nexus.chatapp.model.User
import com.example.nexus.chatapp.utils.LanguageManager
import com.example.nexus.chatapp.utils.LocalStorage
import com.example.nexus.chatapp.utils.ThemeManager
import com.example.nexus.chatapp.utils.hideKeyboard
import com.example.nexus.chatapp.utils.showToast
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var localStorage: LocalStorage
    private lateinit var themeManager: ThemeManager
    private lateinit var languageManager: LanguageManager

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
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupListeners()
    }
    
    private fun setupListeners() {
        // Register button click
        binding.buttonRegister.setOnClickListener {
            attemptRegistration()
        }
        
        // Login link click
        binding.textLogin.setOnClickListener {
            finish() // Go back to login
        }
    }
    
    private fun attemptRegistration() {
        hideKeyboard()
        
        val username = binding.editUsername.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        val confirmPassword = binding.editConfirmPassword.text.toString().trim()
        val fullName = binding.editFullName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        
        // Validate input
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
            fullName.isEmpty() || email.isEmpty()) {
            showToast(getString(com.example.chatapp.R.string.all_fields_required))
            return
        }
        
        if (password != confirmPassword) {
            showToast(getString(com.example.chatapp.R.string.passwords_do_not_match))
            return
        }
        
        if (password.length < 6) {
            showToast(getString(com.example.chatapp.R.string.password_too_short))
            return
        }
        
        // Show progress
        binding.buttonRegister.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            // Check if username already exists
            val existingUser = localStorage.getUserByUsername(username)
            
            if (existingUser != null) {
                binding.progressBar.visibility = android.view.View.GONE
                binding.buttonRegister.isEnabled = true
                showToast(getString(com.example.chatapp.R.string.username_already_exists))
                return@launch
            }
            
            // Create new user
            val userId = localStorage.generateId()
            val newUser = User(
                id = userId,
                username = username,
                password = password,
                fullName = fullName,
                email = email,
                darkModeEnabled = themeManager.isDarkModeEnabled(),
                language = languageManager.getCurrentLanguage()
            )
            
            // Save user
            localStorage.addUser(newUser)
            
            // Save logged in user to preferences
            getSharedPreferences("user_prefs", MODE_PRIVATE).edit().apply {
                putString("current_user_id", userId)
                apply()
            }
            
            binding.progressBar.visibility = android.view.View.GONE
            
            // Navigate to main activity
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
