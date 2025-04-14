package com.example.nexus.chatapp.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nexus.chatapp.MainActivity
import com.example.nexus.chatapp.databinding.ActivityLoginBinding
import com.example.nexus.chatapp.utils.LanguageManager
import com.example.nexus.chatapp.utils.LocalStorage
import com.example.nexus.chatapp.utils.ThemeManager
import com.example.nexus.chatapp.utils.hideKeyboard
import com.example.nexus.chatapp.utils.showToast
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupListeners()
    }
    
    private fun setupListeners() {
        // Login button click
        binding.buttonLogin.setOnClickListener {
            attemptLogin()
        }
        
        // Register link click
        binding.textRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    
    private fun attemptLogin() {
        hideKeyboard()
        
        val username = binding.editUsername.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showToast(getString(com.example.chatapp.R.string.all_fields_required))
            return
        }
        
        // Attempt authentication
        binding.buttonLogin.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            val user = localStorage.authenticate(username, password)
            
            binding.progressBar.visibility = android.view.View.GONE
            binding.buttonLogin.isEnabled = true
            
            if (user != null) {
                // Save logged in user to preferences
                getSharedPreferences("user_prefs", MODE_PRIVATE).edit().apply {
                    putString("current_user_id", user.id)
                    apply()
                }
                
                // Navigate to main activity
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                showToast(getString(com.example.chatapp.R.string.invalid_credentials))
            }
        }
    }
}
