package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.KhmerArtistDirectory
import com.example.data.KhmerArtistProfile
import com.example.data.MediaItem

@Composable
fun HomeScreen(
    mediaItems: List<MediaItem>,
    onPlayTrack: (MediaItem) -> Unit,
    onSelectArtist: (KhmerArtistProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Khmer Artist Directory",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(KhmerArtistDirectory.featuredArtists) { artist ->
                    Column(
                        modifier = Modifier
                            .width(88.dp)
                            .clickable { onSelectArtist(artist) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = artist.imageUrl,
                            contentDescription = artist.englishName,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = artist.englishName,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Trending Today & 2026",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(mediaItems) { track ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayTrack(track) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = track.artworkUrl,
                        contentDescription = track.title,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.licenseBadge,
                            color = Color(0xFF007AFF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = track.artist,
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
