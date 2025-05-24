package com.musicapp.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.composables.MainTopBar
import com.musicapp.ui.composables.TrackCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    mainNavController: NavController,
    subNavController: NavController,
) {
    val searchViewModel: SearchViewModel = koinViewModel()
    val uiState by searchViewModel.uiState.collectAsState()


    Scaffold(
        topBar = { MainTopBar(mainNavController, "Search") },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets)
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search TextField
            OutlinedTextField(
                value = uiState.searchText,
                onValueChange = searchViewModel::onSearchTextChange,
                placeholder = { Text(stringResource(R.string.what_to_play)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = searchViewModel::performSearch) {
                        Icon(
                            Icons.Outlined.Search,
                            stringResource(R.string.search_description)
                        )
                    }
                }
            )

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    )
                }

                uiState.error -> {
                    Text(
                        text = stringResource(R.string.error_search),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }


                uiState.searchResults.tracks.isNotEmpty() -> {
                    LazyColumn(
                        
                    ) {
                        item {
                            val lastSearch = uiState.searchText
                            Text(
                                text = "${stringResource(R.string.search_results)} '${lastSearch}'",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(uiState.searchResults.tracks) { track ->
                            TrackCard(
                                track = track,
                                onTrackClick = { /*TODO*/ },
                                onArtistClick = { /*TODO*/ },
                                extraMenu = {}
                            )
                        }

                    }
                }
                else -> {
                    Text(
                        text = stringResource(R.string.discover_new_things),
                        style = MaterialTheme.typography.titleLarge
                    )

                }
            }
        }
    }
}

/*
* Just an example. Modify as needed.
* */
@Composable
fun GenreItem(title: String, onClick: () -> Unit) {
    Card(
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = "Genre picture",
                modifier = Modifier.size(72.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}