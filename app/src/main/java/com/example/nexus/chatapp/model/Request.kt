package com.example.nexus.chatapp.model

import java.io.Serializable
import java.util.Date

/**
 * Request model representing a task request
 */
data class Request(
    val id: String, // Unique identifier
    val requesterId: String, // User ID of the requester
    val requesterName: String, // Name of the requester
    val assigneeId: String, // User ID of the assignee
    val assigneeName: String, // Name of the assignee
    val requestDate: Long, // Date when the request was created
    val completionDate: Long? = null, // Optional completion deadline
    val taskDescription: String, // Description of the task
    val roomNumber: String, // Room number where the task should be completed
    val status: RequestStatus = RequestStatus.PENDING, // Status of the request
    val comments: MutableList<Comment> = mutableListOf() // Comments on the task
) : Serializable {
    
    fun getFormattedRequestDate(): String {
        return android.text.format.DateFormat.format("dd MMM yyyy", Date(requestDate)).toString()
    }
    
    fun getFormattedCompletionDate(): String {
        return if (completionDate != null) {
            android.text.format.DateFormat.format("dd MMM yyyy", Date(completionDate)).toString()
        } else {
            ""
        }
    }
}

/**
 * Comment model for task requests
 */
data class Comment(
    val id: String, // Unique identifier
    val userId: String, // User ID of the commenter
    val userName: String, // Name of the commenter
    val timestamp: Long, // Comment timestamp
    val text: String // Comment text
) : Serializable {
    
    fun getFormattedTime(): String {
        // Format timestamp to a readable time string
        return android.text.format.DateFormat.format("dd MMM yyyy, HH:mm", Date(timestamp)).toString()
    }
}

/**
 * Enum representing the status of a request
 */
enum class RequestStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
