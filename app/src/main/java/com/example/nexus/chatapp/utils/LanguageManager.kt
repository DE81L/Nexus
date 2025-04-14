package com.example.nexus.chatapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

/**
 * Manages language settings (Russian/English)
 */
class LanguageManager(private val context: Context) {
    
    private val preferences: SharedPreferences = context.getSharedPreferences(
        LANGUAGE_PREFERENCES, Context.MODE_PRIVATE
    )
    
    /**
     * Apply the saved language or default to English
     */
    fun applyLanguage() {
        val languageCode = preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        setLanguage(languageCode)
    }
    
    /**
     * Set the application language
     * @param languageCode Language code (e.g., "en" or "ru")
     */
    fun setLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        context.resources.updateConfiguration(
            configuration,
            context.resources.displayMetrics
        )
        
        preferences.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }
    
    /**
     * Toggle between Russian and English
     * @return The new language code
     */
    fun toggleLanguage(): String {
        val currentLanguage = preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        val newLanguage = if (currentLanguage == LANGUAGE_ENGLISH) LANGUAGE_RUSSIAN else LANGUAGE_ENGLISH
        setLanguage(newLanguage)
        return newLanguage
    }
    
    /**
     * Get the current language code
     * @return Current language code (e.g., "en" or "ru")
     */
    fun getCurrentLanguage(): String {
        return preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
    
    /**
     * Apply language for a specific activity (useful when calling from activities)
     * @param activity Activity to apply the language to
     */
    fun applyLanguageForActivity(activity: AppCompatActivity) {
        val languageCode = preferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(activity.resources.configuration)
        configuration.setLocale(locale)
        
        activity.resources.updateConfiguration(
            configuration,
            activity.resources.displayMetrics
        )
    }
    
    companion object {
        private const val LANGUAGE_PREFERENCES = "language_preferences"
        private const val KEY_LANGUAGE = "language"
        private const val DEFAULT_LANGUAGE = "en"
        
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_RUSSIAN = "ru"
    }
}
