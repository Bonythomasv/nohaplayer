package com.noha.player.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoritesStore @Inject constructor(
    private val context: Context
) {

    private val dataStore: DataStore<Preferences> by lazy {
        androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
        )
    }

    val favoritesFlow: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[FAVORITES_KEY] ?: emptySet()
    }

    suspend fun setFavorites(ids: Set<String>) {
        dataStore.edit { prefs ->
            prefs[FAVORITES_KEY] = ids
        }
    }

    suspend fun toggleFavorite(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY] ?: emptySet()
            prefs[FAVORITES_KEY] = if (current.contains(id)) current - id else current + id
        }
    }

    companion object {
        private const val DATASTORE_NAME = "favorites"
        private val FAVORITES_KEY = stringSetPreferencesKey("favorite_stream_urls")
    }
}

