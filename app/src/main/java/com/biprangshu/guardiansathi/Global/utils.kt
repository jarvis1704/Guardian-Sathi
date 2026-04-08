package com.biprangshu.guardiansathi.Global

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import androidx.core.content.edit

fun setAppLanguage(context: Context, languageCode: String) {
    LanguageUtils.setLocale(context, languageCode)

    // Restart MainActivity to apply the new locale
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    context.startActivity(intent)
}

data class Language(
    val name: String,
    val native: String,
    val code: String
)

// utils/LanguageUtils.kt
object LanguageUtils {

    fun setLocale(context: Context, languageCode: String): Context {
        saveLanguage(context, languageCode)
        return updateResources(context, languageCode)
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }

    private fun saveLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit {
                putString("language", languageCode)
            }
    }

    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}