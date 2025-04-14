package com.example.nexus.chatapp.utils

import android.content.Context
import com.example.nexus.chatapp.model.Message
import com.example.nexus.chatapp.model.Request
import com.example.nexus.chatapp.model.User
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Handles local storage operations using JSON files
 */
class LocalStorage(private val context: Context) {
    
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private var users: MutableList<User> = mutableListOf()
    private var messages: MutableList<Message> = mutableListOf()
    private var requests: MutableList<Request> = mutableListOf()
    
    private val usersFile: File by lazy { File(context.filesDir, "users.json") }
    private val messagesFile: File by lazy { File(context.filesDir, "messages.json") }
    private val requestsFile: File by lazy { File(context.filesDir, "requests.json") }
    
    init {
        // Create the files if they don't exist
        ensureFilesExist()
        
        // Load data from files
        loadData()
    }
    
    private fun ensureFilesExist() {
        if (!usersFile.exists()) {
            usersFile.createNewFile()
            usersFile.writeText("[]")
        }
        
        if (!messagesFile.exists()) {
            messagesFile.createNewFile()
            messagesFile.writeText("[]")
        }
        
        if (!requestsFile.exists()) {
            requestsFile.createNewFile()
            requestsFile.writeText("[]")
        }
    }
    
    private fun loadData() {
        try {
            val usersJson = usersFile.readText()
            val messagesJson = messagesFile.readText()
            val requestsJson = requestsFile.readText()
            
            val userType = object : TypeToken<MutableList<User>>() {}.type
            val messageType = object : TypeToken<MutableList<Message>>() {}.type
            val requestType = object : TypeToken<MutableList<Request>>() {}.type
            
            users = if (usersJson.isNotBlank() && usersJson != "[]") {
                gson.fromJson(usersJson, userType)
            } else {
                mutableListOf()
            }
            
            messages = if (messagesJson.isNotBlank() && messagesJson != "[]") {
                gson.fromJson(messagesJson, messageType)
            } else {
                mutableListOf()
            }
            
            requests = if (requestsJson.isNotBlank() && requestsJson != "[]") {
                gson.fromJson(requestsJson, requestType)
            } else {
                mutableListOf()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Initialize with empty lists in case of an error
            users = mutableListOf()
            messages = mutableListOf()
            requests = mutableListOf()
        }
    }
    
    private suspend fun saveUsers() = withContext(Dispatchers.IO) {
        usersFile.writeText(gson.toJson(users))
    }
    
    private suspend fun saveMessages() = withContext(Dispatchers.IO) {
        messagesFile.writeText(gson.toJson(messages))
    }
    
    private suspend fun saveRequests() = withContext(Dispatchers.IO) {
        requestsFile.writeText(gson.toJson(requests))
    }
    
    // User Operations
    suspend fun getUsers(): List<User> = withContext(Dispatchers.IO) {
        loadData()
        return@withContext users
    }
    
    suspend fun getUserById(userId: String): User? = withContext(Dispatchers.IO) {
        loadData()
        return@withContext users.find { it.id == userId }
    }
    
    suspend fun getUserByUsername(username: String): User? = withContext(Dispatchers.IO) {
        loadData()
        return@withContext users.find { it.username == username }
    }
    
    suspend fun addUser(user: User) = withContext(Dispatchers.IO) {
        loadData()
        users.add(user)
        saveUsers()
    }
    
    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        loadData()
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
            saveUsers()
        }
    }
    
    suspend fun authenticate(username: String, password: String): User? = withContext(Dispatchers.IO) {
        loadData()
        return@withContext users.find { it.username == username && it.password == password }
    }
    
    // Message Operations
    suspend fun getMessages(): List<Message> = withContext(Dispatchers.IO) {
        loadData()
        return@withContext messages
    }
    
    suspend fun getConversationMessages(userId1: String, userId2: String): List<Message> = withContext(Dispatchers.IO) {
        loadData()
        return@withContext messages.filter { 
            (it.senderId == userId1 && it.receiverId == userId2) || 
            (it.senderId == userId2 && it.receiverId == userId1)
        }.sortedBy { it.timestamp }
    }
    
    suspend fun getChatUsers(currentUserId: String): List<User> = withContext(Dispatchers.IO) {
        loadData()
        val userIds = messages.filter {
            it.senderId == currentUserId || it.receiverId == currentUserId
        }.map {
            if (it.senderId == currentUserId) it.receiverId else it.senderId
        }.distinct()
        
        return@withContext users.filter { it.id in userIds }
    }
    
    suspend fun addMessage(message: Message) = withContext(Dispatchers.IO) {
        loadData()
        messages.add(message)
        saveMessages()
    }
    
    suspend fun markMessagesAsRead(senderId: String, receiverId: String) = withContext(Dispatchers.IO) {
        loadData()
        var changed = false
        messages.forEachIndexed { index, message ->
            if (message.senderId == senderId && message.receiverId == receiverId && !message.isRead) {
                messages[index] = message.copy(isRead = true)
                changed = true
            }
        }
        if (changed) saveMessages()
    }
    
    suspend fun getUnreadMessagesCount(userId: String): Map<String, Int> = withContext(Dispatchers.IO) {
        loadData()
        return@withContext messages
            .filter { it.receiverId == userId && !it.isRead }
            .groupBy { it.senderId }
            .mapValues { it.value.size }
    }
    
    // Request Operations
    suspend fun getRequests(): List<Request> = withContext(Dispatchers.IO) {
        loadData()
        return@withContext requests
    }
    
    suspend fun getRequestsByRequesterId(requesterId: String): List<Request> = withContext(Dispatchers.IO) {
        loadData()
        return@withContext requests.filter { it.requesterId == requesterId }
    }
    
    suspend fun getRequestsByAssigneeId(assigneeId: String): List<Request> = withContext(Dispatchers.IO) {
        loadData()
        return@withContext requests.filter { it.assigneeId == assigneeId }
    }
    
    suspend fun getRequestById(requestId: String): Request? = withContext(Dispatchers.IO) {
        loadData()
        return@withContext requests.find { it.id == requestId }
    }
    
    suspend fun addRequest(request: Request) = withContext(Dispatchers.IO) {
        loadData()
        requests.add(request)
        saveRequests()
    }
    
    suspend fun updateRequest(request: Request) = withContext(Dispatchers.IO) {
        loadData()
        val index = requests.indexOfFirst { it.id == request.id }
        if (index != -1) {
            requests[index] = request
            saveRequests()
        }
    }
    
    suspend fun addCommentToRequest(requestId: String, comment: com.example.chatapp.model.Comment) = withContext(Dispatchers.IO) {
        loadData()
        val index = requests.indexOfFirst { it.id == requestId }
        if (index != -1) {
            val request = requests[index]
            val updatedComments = request.comments.toMutableList()
            updatedComments.add(comment)
            requests[index] = request.copy(comments = updatedComments)
            saveRequests()
        }
    }
    
    // Helper functions
    fun generateId(): String = UUID.randomUUID().toString()
}
