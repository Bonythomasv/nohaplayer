package com.noha.player.ui.channel

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.noha.player.R
import com.noha.player.data.model.CategoryItem
import com.noha.player.data.model.Channel
import com.noha.player.data.model.PlaylistEntry
import com.noha.player.ui.player.PlayerActivity
import com.noha.player.ui.components.ParentalPinDialog

@Composable
fun ChannelListScreen(
    viewModel: ChannelListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var autoplayTriggered by rememberSaveable { mutableStateOf(false) }
    var showParentalPin by remember { mutableStateOf(false) }
    var showParentalUnlock by remember { mutableStateOf(false) }
    var showDisclaimer by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.noha_player_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.15f
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NOHA Player",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Settings")
                }
                FilledTonalButton(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add playlist")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add playlist")
                }
            }
        }

        PlaylistSection(
            playlists = uiState.playlists,
            activeId = uiState.activePlaylistId,
            onSelect = { viewModel.setActivePlaylist(it) }
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("Favorites") }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("All") }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) { Text("Categories") }
        }

        if (uiState.playlists.isNotEmpty() && uiState.recentChannels.isNotEmpty()) {
            Text(
                text = "Recents",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.recentChannels) { channel ->
                    ElevatedCard(
                        modifier = Modifier
                            .widthIn(min = 140.dp)
                            .clickable {
                                PlayerActivity.start(context, channel)
                                viewModel.onChannelPlayed(channel)
                            },
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(channel.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                            channel.groupTitle?.let {
                                Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Search bar removed per request; categories tab now uses dropdown only

        val lastPlayed = uiState.lastPlayedChannel
        if (uiState.autoplayLastEnabled && !autoplayTriggered && lastPlayed != null && uiState.filteredChannels.isNotEmpty()) {
            autoplayTriggered = true
            playChannel(
                context = context,
                channel = lastPlayed,
                useExternal = uiState.useExternalPlayer,
                onPlayed = { viewModel.onChannelPlayed(it) }
            )
        }
        
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
            
            else -> {
                val hasPlaylist = uiState.playlists.isNotEmpty()
                if (!hasPlaylist) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add a playlist to load channels.")
                    }
                } else {
                    when (selectedTab) {
                        0 -> {
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
                                            playChannel(
                                                context = context,
                                                channel = channel,
                                                useExternal = uiState.useExternalPlayer,
                                                onPlayed = { viewModel.onChannelPlayed(it) }
                                            )
                                        },
                                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                                        onHideChannel = { viewModel.hideChannel(it) }
                                    )
                                }
                            } else {
                                Text("No favorites yet.")
                            }
                        }
                        1 -> {
                            OutlinedTextField(
                                value = uiState.query,
                                onValueChange = { viewModel.onQueryChange(it) },
                                label = { Text("Search channels") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Search"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                singleLine = true
                            )
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
                                        playChannel(
                                            context = context,
                                            channel = channel,
                                            useExternal = uiState.useExternalPlayer,
                                            onPlayed = { viewModel.onChannelPlayed(it) }
                                        )
                                    },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onHideChannel = { viewModel.hideChannel(it) }
                                )
                            }
                        }
                        2 -> {
                            CategoryDropdown(
                                categories = uiState.categories,
                                selected = uiState.selectedCategory,
                                onSelect = { viewModel.selectCategory(it) }
                            )
                            Spacer(Modifier.height(8.dp))
                            if (uiState.selectedCategory != null) {
                                if (uiState.filteredChannels.isEmpty()) {
                                    Text(
                                        "No channels for this category",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    ChannelList(
                                        channels = uiState.filteredChannels,
                                        favoriteIds = uiState.favoriteIds,
                                        onChannelClick = { channel ->
                                            playChannel(
                                                context = context,
                                                channel = channel,
                                                useExternal = uiState.useExternalPlayer,
                                                onPlayed = { viewModel.onChannelPlayed(it) }
                                            )
                                        },
                                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                                        onHideChannel = { viewModel.hideChannel(it) }
                                    )
                                }
                            } else {
                                Text(
                                    "Select a category from the dropdown",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPlaylistDialog(
            onDismiss = { showAddDialog = false },
            onAddUrl = { name, url ->
                viewModel.addPlaylistUrl(name, url)
                showAddDialog = false
            },
            onAddXtream = { name, base, user, pass ->
                viewModel.addPlaylistXtream(name, base, user, pass)
                showAddDialog = false
            },
            onAddFile = { name, uri ->
                viewModel.addPlaylistUrl(name, uri) // handled by repository for non-http
                showAddDialog = false
            }
        )
    }

    if (showParentalPin) {
        ParentalPinDialog(
            title = "Set parental PIN",
            confirmLabel = "Save",
            onConfirm = {
                viewModel.enableParental(it)
                showParentalPin = false
            },
            onDismiss = { showParentalPin = false }
        )
    }

    if (showParentalUnlock) {
        ParentalPinDialog(
            title = "Unlock parental",
            confirmLabel = "Unlock",
            onConfirm = {
                viewModel.unlockParental(it)
                showParentalUnlock = false
            },
            onDismiss = { showParentalUnlock = false }
        )
    }

    if (showDisclaimer) {
        AlertDialog(
            onDismissRequest = { showDisclaimer = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.acceptDisclaimer()
                    showDisclaimer = false
                }) { Text("I understand") }
            },
            dismissButton = {
                TextButton(onClick = { showDisclaimer = false }) { Text("Cancel") }
            },
            title = { Text("Disclaimer") },
            text = { Text("No built-in channels. User-supplied content only. Do not stream copyrighted content without permission.") }
        )
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) { Text("Close") }
            },
            title = { Text("Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Autoplay last", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = uiState.autoplayLastEnabled, onCheckedChange = { viewModel.setAutoplayLast(it) })
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Start on boot", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = uiState.startOnBoot, onCheckedChange = { viewModel.setStartOnBoot(it) })
                    }
                    TextButton(onClick = { showDisclaimer = true }) { Text("View disclaimer") }
                }
            }
        )
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
private fun PlaylistSection(
    playlists: List<PlaylistEntry>,
    activeId: String?,
    onSelect: (String?) -> Unit
) {
    if (playlists.isEmpty()) {
        Text(
            text = "No playlists. Using default.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        return
    }

    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = "Active playlist",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        var expanded by remember { mutableStateOf(false) }
        val active = playlists.firstOrNull { it.id == activeId } ?: playlists.first()

        Box {
            OutlinedTextField(
                value = active.name,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                playlists.forEach { p ->
                    DropdownMenuItem(
                        text = { Text(p.name) },
                        onClick = {
                            onSelect(p.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddPlaylistDialog(
    onDismiss: () -> Unit,
    onAddUrl: (String?, String) -> Unit,
    onAddXtream: (String?, String, String, String) -> Unit,
    onAddFile: (String?, String) -> Unit
) {
    var tabIndex by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var base by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        fileUri = uri?.toString()
        fileName = uri?.lastPathSegment ?: uri?.toString().orEmpty()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add playlist") },
        text = {
            Column {
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }) { Text("URL") }
                    Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }) { Text("Xtream") }
                    Tab(selected = tabIndex == 2, onClick = { tabIndex = 2 }) { Text("File") }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                when (tabIndex) {
                    0 -> {
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("Playlist URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    1 -> {
                        OutlinedTextField(
                            value = base,
                            onValueChange = { base = it },
                            label = { Text("Base URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = user,
                            onValueChange = { user = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pass,
                            onValueChange = { pass = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    2 -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = { filePicker.launch("*/*") }) {
                                Text("Select file")
                            }
                            Text(
                                text = if (fileName.isNotBlank()) fileName else "No file selected",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (tabIndex) {
                        0 -> if (url.isNotBlank()) onAddUrl(name, url)
                        1 -> if (base.isNotBlank() && user.isNotBlank() && pass.isNotBlank()) onAddXtream(name, base, user, pass)
                        2 -> fileUri?.let { onAddFile(name, it) }
                    }
                }
            ) {
                Text("Save")
            }
        }
    )
}

@Composable
fun ChannelList(
    channels: List<Channel>,
    favoriteIds: Set<String>,
    onChannelClick: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onHideChannel: ((Channel) -> Unit)? = null
) {
    androidx.compose.foundation.lazy.LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(channels) { channel ->
            ChannelItem(
                channel = channel,
                isFavorite = favoriteIds.contains(channel.streamUrl),
                onClick = { onChannelClick(channel) },
                onToggleFavorite = { onToggleFavorite(channel) },
                onHide = onHideChannel?.let { { it(channel) } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<CategoryItem>,
    selected: CategoryItem?,
    onSelect: (CategoryItem?) -> Unit
) {
    if (categories.isEmpty()) {
        Text("No categories available")
        return
    }
    var expanded by remember { mutableStateOf(false) }
    val activeLabel = selected?.let { "${it.name} (${it.count})" } ?: "Select category"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = activeLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categories") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text("${cat.name} (${cat.count})") },
                    onClick = {
                        onSelect(cat)
                        expanded = false
                    }
                )
            }
            if (selected != null) {
                DropdownMenuItem(
                    text = { Text("Clear selection") },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ChannelItem(
    channel: Channel,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onHide: (() -> Unit)? = null
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
            onHide?.let {
                TextButton(onClick = it) {
                    Text("Hide")
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

@Composable
private fun SettingsDialogContent(
    autoplay: Boolean,
    onToggleAutoplay: (Boolean) -> Unit,
    externalPlayer: Boolean,
    onToggleExternal: (Boolean) -> Unit,
    startOnBoot: Boolean,
    onToggleBoot: (Boolean) -> Unit,
    showHidden: Boolean,
    onToggleShowHidden: (Boolean) -> Unit,
    onUnhideAll: () -> Unit,
    parentalEnabled: Boolean,
    parentalUnlocked: Boolean,
    onEnableParental: () -> Unit,
    onDisableParental: () -> Unit,
    onLockParental: () -> Unit,
    onUnlockParental: () -> Unit,
    onDisclaimer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Autoplay last", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = autoplay, onCheckedChange = onToggleAutoplay)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("External player", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = externalPlayer, onCheckedChange = onToggleExternal)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Start on boot", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = startOnBoot, onCheckedChange = onToggleBoot)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Show hidden", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = showHidden, onCheckedChange = onToggleShowHidden)
                TextButton(onClick = onUnhideAll) { Text("Unhide all") }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Parental", style = MaterialTheme.typography.bodyMedium)
                if (parentalEnabled) {
                    if (parentalUnlocked) {
                        TextButton(onClick = onLockParental) { Text("Lock") }
                    } else {
                        TextButton(onClick = onUnlockParental) { Text("Unlock") }
                    }
                    TextButton(onClick = onDisableParental) { Text("Disable") }
                } else {
                    TextButton(onClick = onEnableParental) { Text("Enable") }
                }
            }
            TextButton(onClick = onDisclaimer) { Text("View disclaimer") }
        }
    }
}

private fun playChannel(
    context: android.content.Context,
    channel: Channel,
    useExternal: Boolean,
    onPlayed: (Channel) -> Unit
) {
    if (useExternal) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(channel.streamUrl), "video/*")
        }
        context.startActivity(intent)
    } else {
        PlayerActivity.start(context, channel)
    }
    onPlayed(channel)
}

