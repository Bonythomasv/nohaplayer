package com.noha.player.ui.player

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.noha.player.data.model.Channel
import com.noha.player.ui.player.PlayerScreen
import com.noha.player.ui.theme.NohaPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {
    
    private var exoPlayer: ExoPlayer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController?.let { controller ->
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior = 
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            }
        }
        
        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_CHANNEL, Channel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Channel>(EXTRA_CHANNEL)
        } ?: return finish()
        
        val playerReady = setupPlayer(channel)
        if (!playerReady) return
        
        setContent {
            NohaPlayerTheme {
                exoPlayer?.let { player ->
                    PlayerScreen(
                        channel = channel,
                        exoPlayer = player
                    )
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }
    
    override fun onResume() {
        super.onResume()
        exoPlayer?.play()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
    
    private fun setupPlayer(channel: Channel): Boolean {
        return try {
            exoPlayer = ExoPlayer.Builder(this).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        handlePlaybackError(error)
                    }
                })
                val mediaItem = MediaItem.fromUri(channel.streamUrl)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            }
            true
        } catch (e: Exception) {
            handlePlaybackError(e)
            false
        }
    }

    private fun handlePlaybackError(error: Throwable) {
        // Map common ExoPlayer setup issues to a user-friendly message
        val message = when (error) {
            is PlaybackException -> {
                when (val cause = error.cause) {
                    is HttpDataSource.HttpDataSourceException ->
                        "Stream unavailable or network blocked."
                    else -> error.errorCodeName ?: "Playback error"
                }
            }
            is IllegalStateException -> "Stream type not supported on this device."
            else -> "Unable to start playback: ${error.localizedMessage ?: "Unknown error"}"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        exoPlayer?.release()
        exoPlayer = null
        finish()
    }
    
    companion object {
        private const val EXTRA_CHANNEL = "extra_channel"
        
        fun start(context: Context, channel: Channel) {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL, channel)
            }
            context.startActivity(intent)
        }
    }
}

