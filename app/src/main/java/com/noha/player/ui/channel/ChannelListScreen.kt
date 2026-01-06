package com.noha.player.ui.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.noha.player.data.model.Channel
import com.noha.player.ui.player.PlayerActivity

@Composable
fun ChannelListScreen(
    viewModel: ChannelListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "NohaPlayer",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SearchBar(
            query = uiState.query,
            onQueryChange = { viewModel.onQueryChange(it) },
            onSearch = { viewModel.onSearchClick() },
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                val errorMessage = uiState.error
                ErrorView(
                    error = errorMessage ?: "Unknown error",
                    onRetry = { viewModel.retry() }
                )
            }
            
            uiState.channels.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No channels available")
                }
            }
            
            else -> {
                if (uiState.favoriteChannels.isNotEmpty()) {
                    SectionHeader(
                        title = "Favorites",
                        expanded = uiState.favoritesExpanded,
                        onToggle = { viewModel.toggleFavoritesExpanded() },
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                    if (uiState.favoritesExpanded) {
                        ChannelList(
                            channels = uiState.favoriteChannels,
                            favoriteIds = uiState.favoriteIds,
                            onChannelClick = { channel ->
                                PlayerActivity.start(context, channel)
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                SectionHeader(
                    title = "All Channels",
                    expanded = uiState.allExpanded,
                    onToggle = { viewModel.toggleAllExpanded() },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (uiState.allExpanded) {
                    ChannelList(
                        channels = uiState.filteredChannels,
                        favoriteIds = uiState.favoriteIds,
                        onChannelClick = { channel ->
                            PlayerActivity.start(context, channel)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search channels") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Button(onClick = onSearch) {
            Text("Search")
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onToggle) {
            if (expanded) {
                Icon(
                    imageVector = Icons.Filled.ExpandLess,
                    contentDescription = "Collapse"
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = "Expand"
                )
            }
        }
    }
}

@Composable
fun ChannelList(
    channels: List<Channel>,
    favoriteIds: Set<String>,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(channels) { channel ->
            ChannelItem(
                channel = channel,
                isFavorite = favoriteIds.contains(channel.streamUrl),
                onClick = { onChannelClick(channel) },
                onToggleFavorite = { onToggleFavorite(channel) }
            )
        }
    }
}

@Composable
fun ChannelItem(
    channel: Channel,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Logo
            if (channel.logoUrl != null && channel.logoUrl.isNotEmpty()) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = channel.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            
            // Channel Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (channel.groupTitle != null) {
                    Text(
                        text = channel.groupTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onToggleFavorite) {
                if (isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Unfavorite",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.StarOutline,
                        contentDescription = "Favorite"
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorView(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error loading channels",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

