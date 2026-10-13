package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class LumoThemeOption(
    val id: String,
    val numberLabel: String,
    val displayName: String,
    val subtitle: String,
    val isDark: Boolean
) {
    OBSIDIAN("obsidian", "01", "Obsidian", "Black + cool blue accent", true),
    SNOW("snow", "02", "Snow", "White + crisp slate text", false),
    MIDNIGHT("midnight", "03", "Midnight", "Deep navy + electric violet", true),
    SUNSET("sunset", "04", "Sunset", "Warm volcanic dark + coral orange", true),
    FOREST("forest", "05", "Forest", "Deep botanical green + mint", true);

    companion object {
        fun fromId(id: String): LumoThemeOption =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: OBSIDIAN
    }
}

enum class LumoDestination(
    val route: String,
    val label: String,
    val khmerLabel: String,
    val testTag: String
) {
    HOME("home", "Home", "ទំព័រដើម", "nav_tab_home"),
    SEARCH("search", "Search", "ស្វែងរក", "nav_tab_search"),
    LIBRARY("library", "Library", "បណ្ណាល័យ", "nav_tab_library"),
    DOWNLOADS("downloads", "Downloads", "ទាញយក", "nav_tab_downloads");

    fun displayLabel(language: LumoLanguage): String = when (language) {
        LumoLanguage.KHMER -> khmerLabel
        LumoLanguage.BILINGUAL -> khmerLabel
        LumoLanguage.ENGLISH -> label
    }
}

data class LumoPalette(
    val option: LumoThemeOption,
    val background: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val divider: Color,
    val primaryAccent: Color,
    val homeAccent: Color,
    val searchAccent: Color,
    val libraryAccent: Color,
    val downloadsAccent: Color
) {
    fun accentForTab(destination: LumoDestination): Color = when (destination) {
        LumoDestination.HOME -> homeAccent
        LumoDestination.SEARCH -> searchAccent
        LumoDestination.LIBRARY -> libraryAccent
        LumoDestination.DOWNLOADS -> downloadsAccent
    }
}

// Section 2 & Section 19: Default Dark Theme (Obsidian) + 4 additional curated themes
val ObsidianPalette = LumoPalette(
    option = LumoThemeOption.OBSIDIAN,
    background = Color(0xFF0B0F17),
    surface = Color(0xFF121824),
    elevatedSurface = Color(0xFF182131),
    primaryText = Color(0xFFF5F7FA),
    secondaryText = Color(0xFF8D97A8),
    divider = Color(0xFF252D3A),
    primaryAccent = Color(0xFF3B82F6),
    homeAccent = Color(0xFF3B82F6),      // Blue
    searchAccent = Color(0xFFA855F7),    // Purple
    libraryAccent = Color(0xFF10B981),   // Green
    downloadsAccent = Color(0xFFF97316)  // Orange
)

val SnowPalette = LumoPalette(
    option = LumoThemeOption.SNOW,
    background = Color(0xFFF7F9FC),
    surface = Color(0xFFFFFFFF),
    elevatedSurface = Color(0xFFEEF2F8),
    primaryText = Color(0xFF0B0F17),
    secondaryText = Color(0xFF5A6578),
    divider = Color(0xFFE2E8F0),
    primaryAccent = Color(0xFF2563EB),
    homeAccent = Color(0xFF2563EB),      // Blue
    searchAccent = Color(0xFF9333EA),    // Purple
    libraryAccent = Color(0xFF059669),   // Green
    downloadsAccent = Color(0xFFEA580C)  // Orange
)

val MidnightPalette = LumoPalette(
    option = LumoThemeOption.MIDNIGHT,
    background = Color(0xFF070914),
    surface = Color(0xFF0E1326),
    elevatedSurface = Color(0xFF161E38),
    primaryText = Color(0xFFF3F5FC),
    secondaryText = Color(0xFF8B95B5),
    divider = Color(0xFF222B4A),
    primaryAccent = Color(0xFF8B5CF6),
    homeAccent = Color(0xFF6366F1),      // Indigo-Blue
    searchAccent = Color(0xFFA855F7),    // Violet-Purple
    libraryAccent = Color(0xFF14B8A6),   // Teal-Green
    downloadsAccent = Color(0xFFF59E0B)  // Amber-Orange
)

val SunsetPalette = LumoPalette(
    option = LumoThemeOption.SUNSET,
    background = Color(0xFF120B0E),
    surface = Color(0xFF1C1216),
    elevatedSurface = Color(0xFF28191F),
    primaryText = Color(0xFFFAF5F6),
    secondaryText = Color(0xFFA88D93),
    divider = Color(0xFF36232B),
    primaryAccent = Color(0xFFF97316),
    homeAccent = Color(0xFFFB7185),      // Warm Coral
    searchAccent = Color(0xFFC084FC),    // Orchid Purple
    libraryAccent = Color(0xFF34D399),   // Emerald
    downloadsAccent = Color(0xFFF97316)  // Sunset Orange
)

val ForestPalette = LumoPalette(
    option = LumoThemeOption.FOREST,
    background = Color(0xFF07110D),
    surface = Color(0xFF0D1C16),
    elevatedSurface = Color(0xFF152A21),
    primaryText = Color(0xFFF0FDF6),
    secondaryText = Color(0xFF85A697),
    divider = Color(0xFF1F3B2F),
    primaryAccent = Color(0xFF10B981),
    homeAccent = Color(0xFF38BDF8),      // Sky Blue
    searchAccent = Color(0xFFA78BFA),    // Soft Purple
    libraryAccent = Color(0xFF10B981),   // Forest Mint
    downloadsAccent = Color(0xFFFB923C)  // Warm Orange
)

fun paletteForTheme(option: LumoThemeOption): LumoPalette = when (option) {
    LumoThemeOption.OBSIDIAN -> ObsidianPalette
    LumoThemeOption.SNOW -> SnowPalette
    LumoThemeOption.MIDNIGHT -> MidnightPalette
    LumoThemeOption.SUNSET -> SunsetPalette
    LumoThemeOption.FOREST -> ForestPalette
}
