package com.musicapp.ui.composables

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.musicapp.ui.screens.main.MainCategory

@Composable
fun MainNavBar(
    categories: List<MainCategory>,
    selectedCategory: MainCategory,
    modifier: Modifier = Modifier,
    onCategorySelected: (MainCategory) -> Unit
) {
    NavigationBar(
        modifier = modifier,
    ) {
        categories.forEach { item ->
            NavigationBarItem(
                selected = selectedCategory == item,
                icon = {
                    Icon(
                        imageVector = if (selectedCategory == item) item.primaryIcon else item.secondaryIcon,
                        contentDescription = stringResource(item.stringId)
                    )
                },
                label = { Text(stringResource(item.stringId)) },
                onClick = { onCategorySelected(item) }
            )
        }
    }
}