package com.noha.player.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.noha.player.data.model.Channel

@Composable
fun PlayerScreen(
    channel: Channel,
    exoPlayer: ExoPlayer
) {
    DisposableEffect(Unit) {
        onDispose {
            // Player will be released in Activity's onDestroy
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video Player
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    controllerShowTimeoutMs = 3000
                    controllerAutoShow = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Channel Name Overlay
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            color = Color.Black.copy(alpha = 0.6f),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

