package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf

enum class LumoLanguage(
    val id: String,
    val badgeLabel: String,
    val displayName: String,
    val subtitle: String
) {
    KHMER(
        id = "KM",
        badgeLabel = "ខ្មែរ",
        displayName = "ភាសាខ្មែរ (Khmer)",
        subtitle = "អក្សរខ្មែរពេញលេញ · Full Khmer Language"
    ),
    BILINGUAL(
        id = "BILINGUAL",
        badgeLabel = "ខ្មែរ/EN",
        displayName = "ខ្មែរ + English",
        subtitle = "ពីរភាសា · Bilingual Khmer & English"
    ),
    ENGLISH(
        id = "EN",
        badgeLabel = "EN",
        displayName = "English",
        subtitle = "Standard English Interface"
    );

    fun tr(km: String, en: String, bilingual: String = "$km · $en"): String = when (this) {
        KHMER -> km
        BILINGUAL -> bilingual
        ENGLISH -> en
    }

    fun next(): LumoLanguage = when (this) {
        KHMER -> BILINGUAL
        BILINGUAL -> ENGLISH
        ENGLISH -> KHMER
    }

    companion object {
        fun fromId(id: String): LumoLanguage =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: KHMER
    }
}

val LocalLumoLanguage = compositionLocalOf { LumoLanguage.KHMER }
