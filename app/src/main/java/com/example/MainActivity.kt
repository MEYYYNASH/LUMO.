package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.LumoViewModel
import com.example.ui.components.LiquidGlassBottomNav
import com.example.ui.screens.*

class MainActivity : ComponentActivity() {
    private val viewModel: LumoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF030712)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top App Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "LUMO Hi-Fi",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        // Tab Content
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (viewModel.currentTab) {
                                "home" -> HomeScreen(
                                    mediaItems = viewModel.mediaItems,
                                    onPlayTrack = { viewModel.playTrack(it) },
                                    onSelectArtist = { viewModel.selectedArtist = it }
                                )
                                "search" -> SearchScreen()
                                "library" -> LibraryScreen()
                                "downloads" -> DownloadsScreen()
                                "settings" -> SettingsScreen()
                            }
                        }
                    }

                    // Floating Liquid Glass Bottom Navigation Bar
                    Box(modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)) {
                        LiquidGlassBottomNav(
                            currentTab = viewModel.currentTab,
                            onTabChange = { viewModel.currentTab = it }
                        )
                    }
                }
            }
        }
    }
}
