package com.example.nexus.chatapp.model

import java.io.Serializable
import java.util.Date

/**
 * Message model representing a chat message
 */
data class Message(
    val id: String, // Unique identifier
    val senderId: String, // User ID of the sender
    val receiverId: String, // User ID of the receiver
    val timestamp: Long, // Message timestamp
    val text: String = "", // Message text content
    val imagePath: String = "", // Local path to an image attachment
    val filePath: String = "", // Local path to a file attachment
    val fileName: String = "", // Name of the attached file
    val fileSize: Long = 0L, // Size of the attached file in bytes
    val isRead: Boolean = false // Flag for read status
) : Serializable {
    fun getFormattedTime(): String {
        // Format timestamp to a readable time string
        return android.text.format.DateFormat.format("HH:mm", Date(timestamp)).toString()
    }
    
    fun getFormattedDate(): String {
        // Format timestamp to a readable date string
        return android.text.format.DateFormat.format("dd MMM yyyy", Date(timestamp)).toString()
    }
    
    fun hasAttachment(): Boolean {
        return imagePath.isNotEmpty() || filePath.isNotEmpty()
    }
}
