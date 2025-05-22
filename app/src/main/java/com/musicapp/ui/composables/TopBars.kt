package com.musicapp.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithBackButton(
    navController: NavController,
    modifier: Modifier = Modifier,
    title: String = "",
    action: (@Composable () -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
        },
        expandedHeight = 48.dp,
        navigationIcon = {
            if (navController.previousBackStackEntry != null) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back_description)
                    )
                }
            }
        },
        actions = {
            if (action != null) {
                action()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPlaylistTopBar(
    navController: NavController,
    title: String, modifier: Modifier = Modifier,
    onAddTrack: () -> Unit,
    onEditName: () -> Unit,
    onDeletePlaylist: () -> Unit
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
        },
        navigationIcon = {
            if (navController.previousBackStackEntry != null) {
                IconButton(onClick = {navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.back_description)
                    )
                }
            }
        },
        actions = {
            UserPlaylistDropDownMenu(onAddTrack, onEditName, onDeletePlaylist)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    navController: NavController,
    title: String,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        expandedHeight = 40.dp,
        actions = {
            IconButton(onClick = { navController.navigate(MusicAppRoute.Settings) }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings_description)
                )
            }
        }
    )
}