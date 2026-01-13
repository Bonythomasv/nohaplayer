package com.noha.player.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistEntry(
    val id: String,
    val name: String,
    val type: PlaylistType,
    val url: String,
    val createdAt: Long,
    val lastUsedAt: Long? = null
)

@Serializable
enum class PlaylistType {
    URL,
    FILE,
    XTREAM
}

