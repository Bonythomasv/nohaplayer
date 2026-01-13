package com.noha.player.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SettingsStore @Inject constructor(
    private val context: Context
) {
    private val dataStore: DataStore<Preferences> by lazy {
        androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(DATASTORE_NAME) }
        )
    }

    val autoplayLastFlow: Flow<Boolean> = dataStore.data.map { it[AUTOPLAY_LAST_KEY] ?: false }
    val useExternalPlayerFlow: Flow<Boolean> = dataStore.data.map { it[USE_EXTERNAL_PLAYER_KEY] ?: false }
    val startOnBootFlow: Flow<Boolean> = dataStore.data.map { it[START_ON_BOOT_KEY] ?: false }
    val parentalEnabledFlow: Flow<Boolean> = dataStore.data.map { it[PARENTAL_ENABLED_KEY] ?: false }
    val showHiddenFlow: Flow<Boolean> = dataStore.data.map { it[SHOW_HIDDEN_KEY] ?: false }
    val hiddenChannelsFlow: Flow<Set<String>> = dataStore.data.map { it[HIDDEN_CHANNELS_KEY] ?: emptySet() }
    val disclaimerAcceptedFlow: Flow<Boolean> = dataStore.data.map { it[DISCLAIMER_ACCEPTED_KEY] ?: false }

    val lastPlayedFlow: Flow<LastPlayed?> = dataStore.data.map { prefs ->
        val url = prefs[LAST_URL_KEY]
        val name = prefs[LAST_NAME_KEY]
        val logo = prefs[LAST_LOGO_KEY]
        if (url != null && name != null) LastPlayed(name = name, url = url, logo = logo) else null
    }

    suspend fun setAutoplayLast(enabled: Boolean) {
        dataStore.edit { it[AUTOPLAY_LAST_KEY] = enabled }
    }

    suspend fun setUseExternalPlayer(enabled: Boolean) {
        dataStore.edit { it[USE_EXTERNAL_PLAYER_KEY] = enabled }
    }

    suspend fun setLastPlayed(name: String, url: String, logo: String?) {
        dataStore.edit {
            it[LAST_NAME_KEY] = name
            it[LAST_URL_KEY] = url
            if (logo != null) it[LAST_LOGO_KEY] = logo else it.remove(LAST_LOGO_KEY)
        }
    }

    suspend fun setStartOnBoot(enabled: Boolean) {
        dataStore.edit { it[START_ON_BOOT_KEY] = enabled }
    }

    suspend fun setParental(enabled: Boolean, pin: String?) {
        dataStore.edit {
            it[PARENTAL_ENABLED_KEY] = enabled
            if (pin != null) {
                it[PARENTAL_PIN_KEY] = pin
            } else {
                it.remove(PARENTAL_PIN_KEY)
            }
        }
    }

    suspend fun getParentalPin(): String? = dataStore.data.first()[PARENTAL_PIN_KEY]

    suspend fun setShowHidden(enabled: Boolean) {
        dataStore.edit { it[SHOW_HIDDEN_KEY] = enabled }
    }

    suspend fun hideChannel(url: String) {
        dataStore.edit {
            val set = it[HIDDEN_CHANNELS_KEY] ?: emptySet()
            it[HIDDEN_CHANNELS_KEY] = set + url
        }
    }

    suspend fun unhideAll() {
        dataStore.edit { it[HIDDEN_CHANNELS_KEY] = emptySet() }
    }

    suspend fun acceptDisclaimer() {
        dataStore.edit { it[DISCLAIMER_ACCEPTED_KEY] = true }
    }

    companion object {
        private const val DATASTORE_NAME = "settings"
        private val AUTOPLAY_LAST_KEY = booleanPreferencesKey("autoplay_last")
        private val USE_EXTERNAL_PLAYER_KEY = booleanPreferencesKey("use_external_player")
        private val START_ON_BOOT_KEY = booleanPreferencesKey("start_on_boot")
        private val PARENTAL_ENABLED_KEY = booleanPreferencesKey("parental_enabled")
        private val PARENTAL_PIN_KEY = stringPreferencesKey("parental_pin")
        private val SHOW_HIDDEN_KEY = booleanPreferencesKey("show_hidden_channels")
        private val HIDDEN_CHANNELS_KEY = stringSetPreferencesKey("hidden_channels")
        private val DISCLAIMER_ACCEPTED_KEY = booleanPreferencesKey("disclaimer_accepted")
        private val LAST_NAME_KEY = stringPreferencesKey("last_played_name")
        private val LAST_URL_KEY = stringPreferencesKey("last_played_url")
        private val LAST_LOGO_KEY = stringPreferencesKey("last_played_logo")
    }
}

data class LastPlayed(
    val name: String,
    val url: String,
    val logo: String?
)

