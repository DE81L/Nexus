package com.example.nexus.chatapp.model

import java.io.Serializable

/**
 * User model representing an application user
 */
data class User(
    val id: String, // Unique identifier
    val username: String,
    val password: String,
    val fullName: String,
    val email: String,
    val profilePicturePath: String = "", // Local file path to profile picture if exists
    val darkModeEnabled: Boolean = false,
    val language: String = "en" // "en" or "ru"
) : Serializable
