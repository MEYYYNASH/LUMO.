package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.R

import androidx.compose.runtime.remember
import java.io.File

@Composable
fun LumoArtworkImage(
    artworkUrl: String,
    @DrawableRes fallbackResId: Int = R.drawable.img_album_neon_echoes_1791694225919,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (artworkUrl.isNotBlank()) {
        val imageModel: Any = remember(artworkUrl) {
            when {
                artworkUrl.startsWith("/") -> File(artworkUrl)
                artworkUrl.startsWith("file://") -> File(artworkUrl.removePrefix("file://"))
                else -> artworkUrl
            }
        }
        AsyncImage(
            model = imageModel,
            contentDescription = contentDescription,
            placeholder = painterResource(id = fallbackResId),
            error = painterResource(id = fallbackResId),
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = fallbackResId),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

