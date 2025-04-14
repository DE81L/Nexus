package com.example.nexus.chatapp.chat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexus.chatapp.R
import com.example.nexus.chatapp.databinding.ActivityChatsListBinding
import com.example.nexus.chatapp.model.User
import com.example.nexus.chatapp.utils.LanguageManager
import com.example.nexus.chatapp.utils.LocalStorage
import com.example.nexus.chatapp.utils.ThemeManager
import kotlinx.coroutines.launch

class ChatsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatsListBinding
    private lateinit var localStorage: LocalStorage
    private lateinit var themeManager: ThemeManager
    private lateinit var languageManager: LanguageManager
    private lateinit var adapter: ChatsListAdapter
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
        binding = ActivityChatsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.chats)
        
        // Get current user ID
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("current_user_id", "") ?: ""
        
        if (currentUserId.isEmpty()) {
            finish()
            return
        }
        
        setupRecyclerView()
        loadCurrentUser()
        loadChats()
    }
    
    private fun setupRecyclerView() {
        adapter = ChatsListAdapter(
            onItemClick = { chatUser ->
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("CHAT_USER_ID", chatUser.id)
                    putExtra("CHAT_USER_NAME", chatUser.fullName)
                }
                startActivity(intent)
            }
        )
        
        binding.recyclerChats.layoutManager = LinearLayoutManager(this)
        binding.recyclerChats.adapter = adapter
    }
    
    private fun loadCurrentUser() {
        lifecycleScope.launch {
            val user = localStorage.getUserById(currentUserId)
            if (user != null) {
                currentUser = user
            } else {
                finish()
            }
        }
    }
    
    private fun loadChats() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            // Get unread message counts for notification badges
            val unreadCounts = localStorage.getUnreadMessagesCount(currentUserId)
            
            // Get all users the current user has chatted with
            val chatUsers = localStorage.getChatUsers(currentUserId)
            
            binding.progressBar.visibility = View.GONE
            
            if (chatUsers.isEmpty()) {
                binding.textNoChats.visibility = View.VISIBLE
                binding.recyclerChats.visibility = View.GONE
            } else {
                binding.textNoChats.visibility = View.GONE
                binding.recyclerChats.visibility = View.VISIBLE
                
                // Create chat items with unread count
                val chatItems = chatUsers.map { user ->
                    ChatsListAdapter.ChatItem(
                        user = user,
                        unreadCount = unreadCounts[user.id] ?: 0
                    )
                }
                
                adapter.submitList(chatItems)
            }
        }
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
    
    override fun onResume() {
        super.onResume()
        loadChats() // Refresh the list when returning to this activity
    }
}
