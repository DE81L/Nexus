package com.example.nexus.chatapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.nexus.chatapp.auth.LoginActivity
import com.example.nexus.chatapp.barcode.BarcodeScannerActivity
import com.example.nexus.chatapp.chat.ChatsListActivity
import com.example.nexus.chatapp.databinding.ActivityMainBinding
import com.example.nexus.chatapp.profile.ProfileActivity
import com.example.nexus.chatapp.request.RequestsActivity
import com.example.nexus.chatapp.settings.SettingsActivity
import com.example.nexus.chatapp.utils.LanguageManager
import com.example.nexus.chatapp.utils.ThemeManager

class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Setup click listeners for the main menu items
        binding.cardChats.setOnClickListener {
            startActivity(Intent(this, ChatsListActivity::class.java))
        }
        
        binding.cardRequests.setOnClickListener {
            startActivity(Intent(this, RequestsActivity::class.java))
        }
        
        binding.cardBarcode.setOnClickListener {
            startActivity(Intent(this, BarcodeScannerActivity::class.java))
        }
        
        binding.cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        
        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun logout() {
        // Clear current user from preferences
        getSharedPreferences("user_prefs", MODE_PRIVATE).edit().clear().apply()
        
        // Redirect to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
