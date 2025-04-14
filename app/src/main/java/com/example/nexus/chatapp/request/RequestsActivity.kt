package com.example.nexus.chatapp.request

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexus.chatapp.R
import com.example.nexus.chatapp.chat.ChatActivity
import com.example.nexus.chatapp.databinding.ActivityRequestsBinding
import com.example.nexus.chatapp.model.Request
import com.example.nexus.chatapp.model.User
import com.example.nexus.chatapp.utils.LanguageManager
import com.example.nexus.chatapp.utils.LocalStorage
import com.example.nexus.chatapp.utils.ThemeManager
import com.example.nexus.chatapp.utils.showAlert
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class RequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestsBinding
    private lateinit var localStorage: LocalStorage
    private lateinit var themeManager: ThemeManager
    private lateinit var languageManager: LanguageManager
    private lateinit var adapter: RequestsAdapter
    
    private lateinit var currentUser: User
    private var currentUserId: String = ""
    private var currentFilter: RequestFilter = RequestFilter.ALL
    
    enum class RequestFilter {
        ALL, SENT, RECEIVED
    }
    
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
        binding = ActivityRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.requests)
        
        // Get current user ID
        currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("current_user_id", "") ?: ""
        
        if (currentUserId.isEmpty()) {
            finish()
            return
        }
        
        setupTabLayout()
        setupRecyclerView()
        loadCurrentUser()
        loadRequests()
        
        // Set up FAB to create new request
        binding.fabNewRequest.setOnClickListener {
            showCreateRequestDialog()
        }
    }
    
    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> currentFilter = RequestFilter.ALL
                    1 -> currentFilter = RequestFilter.SENT
                    2 -> currentFilter = RequestFilter.RECEIVED
                }
                loadRequests()
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    
    private fun setupRecyclerView() {
        adapter = RequestsAdapter(
            onItemClick = { request ->
                val intent = Intent(this, RequestDetailActivity::class.java).apply {
                    putExtra("REQUEST_ID", request.id)
                }
                startActivity(intent)
            },
            onChatClick = { userId, userName ->
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("CHAT_USER_ID", userId)
                    putExtra("CHAT_USER_NAME", userName)
                }
                startActivity(intent)
            }
        )
        
        binding.recyclerRequests.layoutManager = LinearLayoutManager(this)
        binding.recyclerRequests.adapter = adapter
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
    
    private fun loadRequests() {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            val requests = when (currentFilter) {
                RequestFilter.ALL -> localStorage.getRequests().filter { 
                    it.requesterId == currentUserId || it.assigneeId == currentUserId
                }
                RequestFilter.SENT -> localStorage.getRequestsByRequesterId(currentUserId)
                RequestFilter.RECEIVED -> localStorage.getRequestsByAssigneeId(currentUserId)
            }
            
            binding.progressBar.visibility = View.GONE
            
            if (requests.isEmpty()) {
                binding.textNoRequests.visibility = View.VISIBLE
                binding.recyclerRequests.visibility = View.GONE
            } else {
                binding.textNoRequests.visibility = View.GONE
                binding.recyclerRequests.visibility = View.VISIBLE
                adapter.submitList(requests)
            }
        }
    }
    
    private fun showCreateRequestDialog() {
        // Instead of creating a dialog here, navigate to the detail activity with no ID
        // This indicates we want to create a new request
        val intent = Intent(this, RequestDetailActivity::class.java).apply {
            putExtra("IS_NEW_REQUEST", true)
        }
        startActivity(intent)
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
        loadRequests() // Refresh the list when returning to this activity
    }
}
