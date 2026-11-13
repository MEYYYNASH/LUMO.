package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiquidGlassBottomNav(
    currentTab: String,
    onTabChange: (String) -> Unit
) {
    val tabs = listOf(
        Triple("home", "Home", Icons.Default.Home),
        Triple("search", "Search", Icons.Default.Search),
        Triple("library", "Library", Icons.Default.LibraryMusic),
        Triple("settings", "Settings", Icons.Default.Settings)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(40.dp))
                .background(Color(0x4C0F172A)) // Liquid Glass background
                .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(40.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { (id, label, icon) ->
                val isActive = currentTab == id
                val contentColor = if (isActive) Color(0xFF007AFF) else Color(0xFF94A3B8)

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(if (isActive) Color(0x4CFFFFFF) else Color.Transparent)
                        .clickable { onTabChange(id) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        color = contentColor,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
