package com.example.nexus.chatapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * Manages theme settings (light/dark mode)
 */
class ThemeManager(context: Context) {
    
    private val preferences: SharedPreferences = context.getSharedPreferences(
        THEME_PREFERENCES, Context.MODE_PRIVATE
    )
    
    /**
     * Apply the saved theme or default to light mode
     */
    fun applyTheme() {
        val isDarkMode = preferences.getBoolean(KEY_DARK_MODE, false)
        setDarkMode(isDarkMode)
    }
    
    /**
     * Set the theme to dark or light mode
     * @param isDarkMode True to set dark mode, false for light mode
     */
    fun setDarkMode(isDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        preferences.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply()
    }
    
    /**
     * Toggle between dark and light mode
     * @return The new state (true if dark mode, false if light mode)
     */
    fun toggleDarkMode(): Boolean {
        val isDarkMode = !preferences.getBoolean(KEY_DARK_MODE, false)
        setDarkMode(isDarkMode)
        return isDarkMode
    }
    
    /**
     * Check if dark mode is enabled
     * @return True if dark mode is enabled, false otherwise
     */
    fun isDarkModeEnabled(): Boolean {
        return preferences.getBoolean(KEY_DARK_MODE, false)
    }
    
    /**
     * Apply theme for a specific activity (useful when calling from activities)
     * @param activity Activity to apply the theme to
     */
    fun applyThemeForActivity(activity: AppCompatActivity) {
        val isDarkMode = preferences.getBoolean(KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    companion object {
        private const val THEME_PREFERENCES = "theme_preferences"
        private const val KEY_DARK_MODE = "dark_mode"
    }
}
