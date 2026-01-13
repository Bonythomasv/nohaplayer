package com.noha.player.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.noha.player.data.model.PlaylistEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

class PlaylistStore @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val dataStore: DataStore<Preferences> by lazy {
        androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
        )
    }

    val playlistsFlow: Flow<List<PlaylistEntry>> = dataStore.data.map { prefs ->
        prefs[PLAYLISTS_KEY]?.let { raw ->
            runCatching { json.decodeFromString<List<PlaylistEntry>>(raw) }.getOrElse { emptyList() }
        } ?: emptyList()
    }

    val activePlaylistIdFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[ACTIVE_PLAYLIST_ID_KEY]
    }

    suspend fun saveNewPlaylist(entry: PlaylistEntry) {
        dataStore.edit { prefs ->
            val current = decode(prefs[PLAYLISTS_KEY])
            prefs[PLAYLISTS_KEY] = json.encodeToString(current + entry)
            prefs[ACTIVE_PLAYLIST_ID_KEY] = entry.id
        }
    }

    suspend fun setActivePlaylist(id: String?) {
        dataStore.edit { prefs ->
            if (id != null) prefs[ACTIVE_PLAYLIST_ID_KEY] = id else prefs.remove(ACTIVE_PLAYLIST_ID_KEY)
        }
    }

    suspend fun updateLastUsed(id: String) {
        dataStore.edit { prefs ->
            val updated = decode(prefs[PLAYLISTS_KEY]).map { p ->
                if (p.id == id) p.copy(lastUsedAt = System.currentTimeMillis()) else p
            }
            prefs[PLAYLISTS_KEY] = json.encodeToString(updated)
        }
    }

    private fun decode(raw: String?): List<PlaylistEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<PlaylistEntry>>(raw) }.getOrElse { emptyList() }
    }

    companion object {
        private const val DATASTORE_NAME = "playlists"
        private val PLAYLISTS_KEY = stringPreferencesKey("playlists_json")
        private val ACTIVE_PLAYLIST_ID_KEY = stringPreferencesKey("active_playlist_id")
    }
}

