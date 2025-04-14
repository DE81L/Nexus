package com.example.nexus.chatapp.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nexus.chatapp.R
import com.example.nexus.chatapp.databinding.ActivityChatBinding
import com.example.nexus.chatapp.model.Message
import com.example.nexus.chatapp.model.User
import com.example.nexus.chatapp.utils.*
import kotlinx.coroutines.launch
import java.io.File

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var localStorage: LocalStorage
    private lateinit var themeManager: ThemeManager
    private lateinit var languageManager: LanguageManager
    private lateinit var adapter: ChatAdapter
    
    private lateinit var currentUser: User
    private var chatUserId: String = ""
    private var chatUserName: String = ""
    private var currentPhotoPath: String = ""
    private var currentFilePath: String = ""
    private var currentFileName: String = ""
    private var currentFileSize: Long = 0L
    
    // Activity result launchers
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Image captured successfully
            if (currentPhotoPath.isNotEmpty()) {
                sendImageMessage(currentPhotoPath)
            }
        }
    }
    
    private val selectFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val fileName = FileUtils.getFileNameFromUri(this, uri) ?: "file"
                val fileSize = FileUtils.getFileSizeFromUri(this, uri)
                
                // Copy file to app's internal storage
                val file = FileUtils.copyFileFromUri(this, uri)
                if (file != null) {
                    sendFileMessage(file.absolutePath, fileName, fileSize)
                } else {
                    showToast(getString(R.string.error_processing_file))
                }
            }
        }
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
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Get chat user information
        chatUserId = intent.getStringExtra("CHAT_USER_ID") ?: ""
        chatUserName = intent.getStringExtra("CHAT_USER_NAME") ?: getString(R.string.chat)
        
        if (chatUserId.isEmpty()) {
            finish()
            return
        }
        
        supportActionBar?.title = chatUserName
        
        // Get current user ID
        val currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("current_user_id", "") ?: ""
        
        if (currentUserId.isEmpty()) {
            finish()
            return
        }
        
        setupRecyclerView()
        setupClickListeners()
        loadCurrentUser(currentUserId)
        loadMessages(currentUserId, chatUserId)
        markMessagesAsRead(currentUserId, chatUserId)
    }
    
    private fun setupRecyclerView() {
        adapter = ChatAdapter(
            onImageClick = { imagePath ->
                // Open image in a viewer
                val file = File(imagePath)
                if (file.exists()) {
                    openFile(file)
                }
            },
            onFileClick = { filePath ->
                // Open file
                val file = File(filePath)
                if (file.exists()) {
                    openFile(file)
                }
            }
        )
        
        binding.recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Messages appear from bottom to top
        }
        binding.recyclerMessages.adapter = adapter
    }
    
    private fun setupClickListeners() {
        // Send message button
        binding.buttonSend.setOnClickListener {
            sendTextMessage()
        }
        
        // Attach file button
        binding.buttonAttach.setOnClickListener {
            selectFile()
        }
        
        // Take photo button
        binding.buttonCamera.setOnClickListener {
            takePhoto()
        }
    }
    
    private fun loadCurrentUser(userId: String) {
        lifecycleScope.launch {
            val user = localStorage.getUserById(userId)
            if (user != null) {
                currentUser = user
            } else {
                finish()
            }
        }
    }
    
    private fun loadMessages(currentUserId: String, chatUserId: String) {
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            val messages = localStorage.getConversationMessages(currentUserId, chatUserId)
            
            binding.progressBar.visibility = View.GONE
            
            if (messages.isEmpty()) {
                binding.textNoMessages.visibility = View.VISIBLE
            } else {
                binding.textNoMessages.visibility = View.GONE
                adapter.submitList(messages)
                binding.recyclerMessages.scrollToPosition(messages.size - 1)
            }
        }
    }
    
    private fun markMessagesAsRead(currentUserId: String, senderId: String) {
        lifecycleScope.launch {
            localStorage.markMessagesAsRead(senderId, currentUserId)
        }
    }
    
    private fun sendTextMessage() {
        val messageText = binding.editMessage.text.toString().trim()
        if (messageText.isEmpty()) return
        
        binding.editMessage.text?.clear()
        
        // Create and save the message
        val messageId = localStorage.generateId()
        val message = Message(
            id = messageId,
            senderId = currentUser.id,
            receiverId = chatUserId,
            timestamp = System.currentTimeMillis(),
            text = messageText
        )
        
        lifecycleScope.launch {
            localStorage.addMessage(message)
            loadMessages(currentUser.id, chatUserId)
        }
    }
    
    private fun sendImageMessage(imagePath: String) {
        // Create and save the message with image
        val messageId = localStorage.generateId()
        val message = Message(
            id = messageId,
            senderId = currentUser.id,
            receiverId = chatUserId,
            timestamp = System.currentTimeMillis(),
            imagePath = imagePath
        )
        
        lifecycleScope.launch {
            localStorage.addMessage(message)
            loadMessages(currentUser.id, chatUserId)
        }
    }
    
    private fun sendFileMessage(filePath: String, fileName: String, fileSize: Long) {
        // Create and save the message with file
        val messageId = localStorage.generateId()
        val message = Message(
            id = messageId,
            senderId = currentUser.id,
            receiverId = chatUserId,
            timestamp = System.currentTimeMillis(),
            filePath = filePath,
            fileName = fileName,
            fileSize = fileSize
        )
        
        lifecycleScope.launch {
            localStorage.addMessage(message)
            loadMessages(currentUser.id, chatUserId)
        }
    }
    
    private fun takePhoto() {
        val photoFile = try {
            FileUtils.createImageFile(this)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(getString(R.string.error_creating_image_file))
            null
        }
        
        photoFile?.let {
            currentPhotoPath = it.absolutePath
            val photoURI = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }
            
            takePictureLauncher.launch(intent)
        }
    }
    
    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        
        selectFileLauncher.launch(Intent.createChooser(intent, getString(R.string.select_file)))
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
        // Mark messages as read when the user returns to the chat
        markMessagesAsRead(currentUser.id, chatUserId)
    }
}
