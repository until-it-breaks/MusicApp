package com.musicapp.ui.composables

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.musicapp.ui.screens.main.MainCategory

@Composable
fun MainNavBar(items: List<MainCategory>, selectedItem: MainCategory, onItemSelected: (MainCategory) -> Unit) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                icon = {
                    Icon(
                        imageVector = if (selectedItem == item) item.primaryIcon else item.secondaryIcon,
                        contentDescription = stringResource(item.stringId)
                    )
                },
                label = { Text(stringResource(item.stringId)) },
                onClick = { onItemSelected(item) }
            )
        }
    }
}