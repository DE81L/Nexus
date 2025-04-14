package com.example.nexus.chatapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.nexus.chatapp.auth.LoginActivity
import com.example.nexus.chatapp.databinding.ActivitySplashBinding
import com.example.nexus.chatapp.utils.LanguageManager
import com.example.nexus.chatapp.utils.ThemeManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var themeManager: ThemeManager
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize managers
        themeManager = ThemeManager(this)
        languageManager = LanguageManager(this)
        
        // Apply theme and language
        themeManager.applyThemeForActivity(this)
        languageManager.applyLanguageForActivity(this)
        
        // Use view binding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Navigate after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, SPLASH_DELAY)
    }
    
    private fun navigateToNextScreen() {
        // Check if the user is logged in
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = prefs.getString("current_user_id", null)
        
        val intent = if (userId != null) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        
        // Start next activity
        startActivity(intent)
        
        // Finish this activity
        finish()
    }
    
    companion object {
        private const val SPLASH_DELAY = 1500L // 1.5 seconds
    }
}
