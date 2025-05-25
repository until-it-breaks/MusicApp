package com.musicapp.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.musicapp.data.models.Theme
import com.musicapp.ui.MusicAppRoute
import com.musicapp.ui.composables.TopBarWithBackButton
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(navController: NavController) {
    val viewModel = koinViewModel<SettingsViewModel>()

    val allowExplicit by viewModel.allowExplicit.collectAsState()
    val currentTheme by viewModel.theme.collectAsState()

    val themeOptions: Map<Theme, Int> = mapOf(
        Theme.Default to R.string.theme_default,
        Theme.Light to R.string.theme_light,
        Theme.Dark to R.string.theme_dark,
    )

    Scaffold(
        topBar = {
            TopBarWithBackButton(
                navController = navController,
                title = stringResource(R.string.account_and_settings)
            )
        }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            Card(
                onClick = { navController.navigate(MusicAppRoute.Profile) }
            ) {
                Row(
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
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(R.string.view_profile),
                            style = MaterialTheme.typography.bodyMedium
                        )
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
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.allow_explicit),
                                style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stringResource(R.string.allow_explicit_description),
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = allowExplicit,
                            onCheckedChange = { viewModel.setAllowExplicit(it) }
                        )
                    }
                }
                HorizontalDivider()
                Card(
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.theme),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.theme_description),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(text = stringResource(id = themeOptions[currentTheme] ?: R.string.theme_default))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                themeOptions.forEach { (theme, labelResId) ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = labelResId)) },
                                        onClick = {
                                            viewModel.setTheme(theme)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}