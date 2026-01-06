package com.noha.player.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noha.player.data.local.FavoritesStore
import com.noha.player.data.model.Channel
import com.noha.player.data.repository.IPTVRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChannelListUiState(
    val channels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val favoriteChannels: List<Channel> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val favoritesExpanded: Boolean = true,
    val allExpanded: Boolean = true,
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChannelListViewModel @Inject constructor(
    private val repository: IPTVRepository,
    private val favoritesStore: FavoritesStore
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChannelListUiState(isLoading = true))
    val uiState: StateFlow<ChannelListUiState> = _uiState.asStateFlow()
    
    init {
        loadChannels()
        observeFavorites()
    }
    
    fun loadChannels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.fetchChannels().collect { result ->
                result.fold(
                    onSuccess = { channels ->
                        val currentFavorites = _uiState.value.favoriteIds
                        val favoriteChannels = channels.filter { currentFavorites.contains(it.streamUrl) }
                        _uiState.value = ChannelListUiState(
                            channels = channels,
                            filteredChannels = channels,
                            favoriteChannels = favoriteChannels,
                            favoriteIds = currentFavorites,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = ChannelListUiState(
                            channels = emptyList(),
                            isLoading = false,
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

    fun onQueryChange(query: String) {
        val currentChannels = _uiState.value.channels
        _uiState.value = _uiState.value.copy(
            query = query,
            filteredChannels = filterChannels(currentChannels, query)
        )
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

    private fun filterChannels(channels: List<Channel>, query: String): List<Channel> {
        if (query.isBlank()) return channels
        val lower = query.trim().lowercase()
        return channels.filter { channel ->
            channel.name.lowercase().contains(lower) ||
                (channel.groupTitle?.lowercase()?.contains(lower) ?: false) ||
                (channel.country?.lowercase()?.contains(lower) ?: false) ||
                (channel.language?.lowercase()?.contains(lower) ?: false)
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesStore.favoritesFlow.collect { ids ->
                val favorites = _uiState.value.channels.filter { ids.contains(it.streamUrl) }
                _uiState.value = _uiState.value.copy(
                    favoriteIds = ids,
                    favoriteChannels = favorites,
                    filteredChannels = filterChannels(_uiState.value.channels, _uiState.value.query)
                )
            }
        }
    }
}

