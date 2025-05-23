package com.musicapp.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.musicapp.R
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.TopBarWithBackButton

@Composable
fun SettingsScreen(navController: NavController) {
    var setting1 by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { TopBarWithBackButton(navController, title = stringResource(R.string.account_and_settings)) }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Card(
                onClick = { navController.navigate(MusicAppRoute.Profile) }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        imageVector = Icons.Filled.Image,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(72.dp)
                    )
                    Column {
                        Text(
                            text = "Profile Name",
                            style = MaterialTheme.typography.headlineSmall)
                        Text(
                            text = stringResource(R.string.view_profile),
                            style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            Column {
                Card(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.allow_explicit_songs),
                                style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stringResource(R.string.allow_explicit_songs_description),
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = setting1,
                            onCheckedChange = { setting1 = it }
                        )
                    }
                }
                HorizontalDivider()
                Card(
                    shape = RoundedCornerShape(0)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Allow explicit songs",
                                style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Songs containing explicit content won't be shown",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = setting1,
                            onCheckedChange = { setting1 = it }
                        )
                    }
                }
                HorizontalDivider()
                Card(
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Allow explicit songs",
                                style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Songs containing explicit content won't be shown",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = setting1,
                            onCheckedChange = { setting1 = it }
                        )
                    }
                }
            }
        }
    }
}