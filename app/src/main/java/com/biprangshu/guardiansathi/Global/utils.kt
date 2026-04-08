package com.biprangshu.guardiansathi.Global

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat


fun setAppLanguage(langCode: String) {
    val localeList = LocaleListCompat.forLanguageTags(langCode)
    AppCompatDelegate.setApplicationLocales(localeList)
}