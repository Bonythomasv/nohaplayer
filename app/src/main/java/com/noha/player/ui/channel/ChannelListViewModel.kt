package com.noha.player.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noha.player.data.local.FavoritesStore
import com.noha.player.data.local.PlaylistStore
import com.noha.player.data.local.SettingsStore
import com.noha.player.data.model.CategoryItem
import com.noha.player.data.model.CategoryType
import com.noha.player.data.model.Channel
import com.noha.player.data.model.PlaylistEntry
import com.noha.player.data.model.PlaylistType
import com.noha.player.data.repository.IPTVRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChannelListUiState(
    val channels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val favoriteChannels: List<Channel> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val playlists: List<PlaylistEntry> = emptyList(),
    val activePlaylistId: String? = null,
    val recentPlaylists: List<PlaylistEntry> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val filteredCategories: List<CategoryItem> = emptyList(),
    val selectedCategory: CategoryItem? = null,
    val recentChannels: List<Channel> = emptyList(),
    val autoplayLastEnabled: Boolean = false,
    val useExternalPlayer: Boolean = false,
    val lastPlayedChannel: Channel? = null,
    val startOnBoot: Boolean = false,
    val parentalEnabled: Boolean = false,
    val parentalUnlocked: Boolean = false,
    val showHidden: Boolean = false,
    val hiddenChannelIds: Set<String> = emptySet(),
    val disclaimerAccepted: Boolean = false,
    val favoritesExpanded: Boolean = true,
    val allExpanded: Boolean = true,
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChannelListViewModel @Inject constructor(
    private val repository: IPTVRepository,
    private val favoritesStore: FavoritesStore,
    private val playlistStore: PlaylistStore,
    private val settingsStore: SettingsStore
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChannelListUiState(isLoading = true))
    val uiState: StateFlow<ChannelListUiState> = _uiState.asStateFlow()
    
    init {
        loadChannels()
        observeFavorites()
        observePlaylists()
        observeSettings()
    }
    
    fun loadChannels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val url = resolveActivePlaylistUrl()
            repository.fetchChannels(url).collect { result ->
                result.fold(
                    onSuccess = { channels ->
                        val currentFavorites = _uiState.value.favoriteIds
                        val favoriteChannels = channels.filter { currentFavorites.contains(it.streamUrl) }
                        val categories = buildCategories(channels)
                        _uiState.value = ChannelListUiState(
                            channels = channels,
                            filteredChannels = filterChannels(
                                channels,
                                _uiState.value.query,
                                _uiState.value.selectedCategory,
                                _uiState.value.showHidden,
                                _uiState.value.hiddenChannelIds
                            ),
                            favoriteChannels = favoriteChannels,
                            favoriteIds = currentFavorites,
                            playlists = _uiState.value.playlists,
                            recentPlaylists = _uiState.value.recentPlaylists,
                            activePlaylistId = _uiState.value.activePlaylistId,
                            categories = categories,
                            filteredCategories = filterCategoriesList(categories, _uiState.value.query),
                            selectedCategory = _uiState.value.selectedCategory,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = ChannelListUiState(
                            channels = emptyList(),
                            isLoading = false,
                            playlists = _uiState.value.playlists,
                            recentPlaylists = _uiState.value.recentPlaylists,
                            activePlaylistId = _uiState.value.activePlaylistId,
                            categories = _uiState.value.categories,
                            filteredCategories = _uiState.value.filteredCategories,
                            selectedCategory = _uiState.value.selectedCategory,
                            error = exception.message ?: "Unknown error occurred"
                        )
                    }
                )
            }
        }
    }
    
    fun retry() {
        loadChannels()
    }

    fun onChannelPlayed(channel: Channel) {
        viewModelScope.launch {
            _uiState.value.activePlaylistId?.let { playlistStore.updateLastUsed(it) }
            settingsStore.setLastPlayed(channel.name, channel.streamUrl, channel.logoUrl)
        }
        val updatedRecents = (listOf(channel) + _uiState.value.recentChannels.filter { it.streamUrl != channel.streamUrl })
            .take(10)
        _uiState.value = _uiState.value.copy(recentChannels = updatedRecents)
    }

    fun onQueryChange(query: String) {
        // All-tab search: reset category filter while applying query
        applyFilters(query = query, category = null)
    }

    fun onSearchClick() {
        // Explicit search trigger to mirror the button behavior; reuses same filter logic.
        onQueryChange(_uiState.value.query)
    }

    fun toggleFavorite(channel: Channel) {
        val key = channel.streamUrl
        viewModelScope.launch {
            favoritesStore.toggleFavorite(key)
        }
    }

    fun toggleFavoritesExpanded() {
        _uiState.value = _uiState.value.copy(
            favoritesExpanded = !_uiState.value.favoritesExpanded
        )
    }

    fun toggleAllExpanded() {
        _uiState.value = _uiState.value.copy(
            allExpanded = !_uiState.value.allExpanded
        )
    }

    fun selectCategory(category: CategoryItem?) {
        // Category-tab selection: ignore text query to avoid empty results when query is non-matching
        applyFilters(query = "", category = category)
    }

    fun addPlaylistUrl(name: String?, url: String) {
        val entry = PlaylistEntry(
            id = UUID.randomUUID().toString(),
            name = name?.ifBlank { null } ?: "Playlist",
            type = PlaylistType.URL,
            url = url,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            playlistStore.saveNewPlaylist(entry)
        }
    }

    fun addPlaylistXtream(name: String?, baseUrl: String, username: String, password: String) {
        val formattedBase = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
        val m3uUrl = "$formattedBase/get.php?username=$username&password=$password&type=m3u"
        addPlaylistUrl(name ?: "Xtream", m3uUrl)
    }

    fun setActivePlaylist(id: String?) {
        viewModelScope.launch {
            playlistStore.setActivePlaylist(id)
            id?.let { playlistStore.updateLastUsed(it) }
            loadChannels()
        }
    }

    fun setAutoplayLast(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setAutoplayLast(enabled) }
    }

    fun setUseExternalPlayer(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setUseExternalPlayer(enabled) }
    }

    fun setStartOnBoot(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setStartOnBoot(enabled) }
    }

    fun setShowHidden(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setShowHidden(enabled) }
    }

    fun hideChannel(channel: Channel) {
        viewModelScope.launch { settingsStore.hideChannel(channel.streamUrl) }
    }

    fun unhideAll() {
        viewModelScope.launch { settingsStore.unhideAll() }
    }

    fun enableParental(pin: String) {
        viewModelScope.launch { settingsStore.setParental(true, pin) }
    }

    fun disableParental() {
        viewModelScope.launch { settingsStore.setParental(false, null) }
    }

    fun unlockParental(pin: String) {
        viewModelScope.launch {
            val stored = settingsStore.getParentalPin()
            if (stored != null && stored == pin) {
                _uiState.value = _uiState.value.copy(parentalUnlocked = true)
            }
        }
    }

    fun lockParental() {
        _uiState.value = _uiState.value.copy(parentalUnlocked = false)
    }

    fun acceptDisclaimer() {
        viewModelScope.launch { settingsStore.acceptDisclaimer() }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesStore.favoritesFlow.collect { ids ->
                val favorites = _uiState.value.channels.filter { ids.contains(it.streamUrl) }
                _uiState.value = _uiState.value.copy(
                    favoriteIds = ids,
                    favoriteChannels = favorites,
                    filteredChannels = filterChannels(
                        _uiState.value.channels,
                        _uiState.value.query,
                        _uiState.value.selectedCategory,
                        _uiState.value.showHidden,
                        _uiState.value.hiddenChannelIds
                    )
                )
            }
        }
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            playlistStore.playlistsFlow.collectLatest { playlists ->
                _uiState.value = _uiState.value.copy(
                    playlists = playlists,
                    recentPlaylists = playlists.sortedByDescending { it.lastUsedAt ?: it.createdAt }.take(5)
                )
            }
        }
        viewModelScope.launch {
            playlistStore.activePlaylistIdFlow.collectLatest { activeId ->
                _uiState.value = _uiState.value.copy(activePlaylistId = activeId)
            }
        }
    }

    private fun resolveActivePlaylistUrl(): String {
        val activeId = _uiState.value.activePlaylistId
        val active = _uiState.value.playlists.firstOrNull { it.id == activeId }
        return active?.url ?: IPTVRepository.PLAYLIST_URLS.first()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsStore.autoplayLastFlow.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(autoplayLastEnabled = enabled)
            }
        }
        viewModelScope.launch {
            settingsStore.useExternalPlayerFlow.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(useExternalPlayer = enabled)
            }
        }
        viewModelScope.launch {
            settingsStore.startOnBootFlow.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(startOnBoot = enabled)
            }
        }
        viewModelScope.launch {
            settingsStore.parentalEnabledFlow.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(parentalEnabled = enabled, parentalUnlocked = !enabled)
            }
        }
        viewModelScope.launch {
            settingsStore.showHiddenFlow.collectLatest { enabled ->
                applyFilters(query = _uiState.value.query, category = _uiState.value.selectedCategory, showHidden = enabled)
            }
        }
        viewModelScope.launch {
            settingsStore.hiddenChannelsFlow.collectLatest { hidden ->
                _uiState.value = _uiState.value.copy(hiddenChannelIds = hidden)
                applyFilters(query = _uiState.value.query, category = _uiState.value.selectedCategory, hiddenIds = hidden)
            }
        }
        viewModelScope.launch {
            settingsStore.lastPlayedFlow.collectLatest { last ->
                val channel = last?.let {
                    Channel(
                        name = it.name,
                        streamUrl = it.url,
                        logoUrl = it.logo
                    )
                }
                _uiState.value = _uiState.value.copy(lastPlayedChannel = channel)
            }
        }
        viewModelScope.launch {
            settingsStore.disclaimerAcceptedFlow.collectLatest { accepted ->
                _uiState.value = _uiState.value.copy(disclaimerAccepted = accepted)
            }
        }
    }

    private fun buildCategories(channels: List<Channel>): List<CategoryItem> {
        val groupCounts = mutableMapOf<String, Int>()
        val countryCounts = mutableMapOf<String, Int>()
        channels.forEach { ch ->
            val groups = ch.groupTitle
                ?.split(';', ',', '|')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.ifEmpty { listOf("Undefined") }
                ?: listOf("Undefined")

            val countries = ch.country
                ?.split(';', ',', '|')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.ifEmpty { listOf("Unknown") }
                ?: listOf("Unknown")

            groups.forEach { g -> groupCounts[g] = (groupCounts[g] ?: 0) + 1 }
            countries.forEach { c -> countryCounts[c] = (countryCounts[c] ?: 0) + 1 }
        }
        val groups = groupCounts.map { CategoryItem(it.key, CategoryType.GROUP, it.value) }
        val countries = countryCounts.map { CategoryItem(it.key, CategoryType.COUNTRY, it.value) }
        return (groups + countries).sortedBy { it.name.lowercase() }
    }

    private fun filterCategoriesList(categories: List<CategoryItem>, query: String): List<CategoryItem> {
        if (query.isBlank()) return categories
        val lower = query.trim().lowercase()
        return categories.filter { it.name.lowercase().contains(lower) }
    }

    private fun filterChannels(
        channels: List<Channel>,
        query: String,
        category: CategoryItem?,
        showHidden: Boolean,
        hiddenIds: Set<String>
    ): List<Channel> {
        val base = if (query.isBlank()) channels else {
            val lower = query.trim().lowercase()
            channels.filter { channel ->
                channel.name.lowercase().contains(lower) ||
                    (channel.groupTitle?.lowercase()?.contains(lower) ?: false) ||
                    (channel.country?.lowercase()?.contains(lower) ?: false) ||
                    (channel.language?.lowercase()?.contains(lower) ?: false)
            }
        }
        val catFiltered = category?.let { cat ->
            when (cat.type) {
                CategoryType.GROUP -> {
                    base.filter { ch ->
                        val groups = ch.groupTitle
                            ?.split(';', ',', '|')
                            ?.map { it.trim().lowercase() }
                            ?.filter { it.isNotEmpty() }
                            ?: listOf("undefined")
                        groups.contains(cat.name.lowercase())
                    }
                }
                CategoryType.COUNTRY -> {
                    base.filter { ch ->
                        val countries = ch.country
                            ?.split(';', ',', '|')
                            ?.map { it.trim().lowercase() }
                            ?.filter { it.isNotEmpty() }
                            ?: listOf("unknown")
                        countries.contains(cat.name.lowercase())
                    }
                }
            }
        } ?: base
        return if (showHidden) catFiltered else catFiltered.filterNot { hiddenIds.contains(it.streamUrl) }
    }

    private fun applyFilters(
        query: String,
        category: CategoryItem?,
        showHidden: Boolean = _uiState.value.showHidden,
        hiddenIds: Set<String> = _uiState.value.hiddenChannelIds
    ) {
        val categories = _uiState.value.categories.ifEmpty { buildCategories(_uiState.value.channels) }
        val filteredCats = filterCategoriesList(categories, query)
        val filteredCh = filterChannels(_uiState.value.channels, query, category, showHidden, hiddenIds)
        _uiState.value = _uiState.value.copy(
            query = query,
            selectedCategory = category,
            categories = categories,
            filteredCategories = filteredCats,
            filteredChannels = filteredCh,
            showHidden = showHidden
        )
    }
}

