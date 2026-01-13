package com.noha.player.data.repository

import android.content.Context
import com.noha.player.data.model.Channel
import com.noha.player.data.parser.M3UParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IPTVRepository @Inject constructor(
    private val iptvService: IPTVService,
    private val context: Context
) {
    companion object {
        // Primary GitHub Pages endpoint and a raw GitHub fallback for DNS issues on emulators
        val PLAYLIST_URLS = listOf(
            "https://iptv-org.github.io/iptv/index.m3u",
            "https://raw.githubusercontent.com/iptv-org/iptv/master/index.m3u"
        )
    }
    
    fun fetchChannels(url: String = PLAYLIST_URLS.first()): Flow<Result<List<Channel>>> = flow {
        var lastError: Exception? = null
        val candidates = if (url.isNotBlank()) listOf(url) + PLAYLIST_URLS else PLAYLIST_URLS
        for (candidate in candidates.distinct()) {
            val result = runCatching {
                if (candidate.startsWith("http", ignoreCase = true)) {
                    iptvService.fetchPlaylist(candidate)
                } else {
                    // Local/file content
                    context.contentResolver.openInputStream(android.net.Uri.parse(candidate))?.use { input ->
                        input.reader().readText()
                    } ?: throw IllegalStateException("Unable to open playlist")
                }
            }
            if (result.isSuccess) {
                val channels = M3UParser.parse(result.getOrThrow())
                emit(Result.success(channels))
                return@flow
            } else {
                lastError = (result.exceptionOrNull() ?: Exception("Unknown error")) as Exception?
            }
        }
        emit(Result.failure(lastError ?: Exception("Failed to load playlist")))
    }
}

