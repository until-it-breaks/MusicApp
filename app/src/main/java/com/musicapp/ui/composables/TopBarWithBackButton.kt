package com.musicapp.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.musicapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithBackButton(navController: NavController, title: String, modifier: Modifier = Modifier) {
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
        }
    )
}