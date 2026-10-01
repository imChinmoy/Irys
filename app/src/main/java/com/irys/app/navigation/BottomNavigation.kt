package com.irys.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem(Routes.HOME, "Home", Icons.Default.Home)
    data object Nearby : BottomNavItem(Routes.NEARBY, "Nearby", Icons.Default.Bluetooth)
    data object Chats : BottomNavItem(Routes.CHATS, "Chats", Icons.AutoMirrored.Filled.Chat)
    data object Emergency : BottomNavItem(Routes.EMERGENCY, "Emergency", Icons.Default.Warning)
    data object Settings : BottomNavItem(Routes.SETTINGS, "Settings", Icons.Default.Settings)

    companion object {
        val items = listOf(Home, Nearby, Chats, Emergency, Settings)
    }
}

@Composable
fun IrysBottomNavigationBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        BottomNavItem.items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        onNavigateToRoute(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(text = item.title)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (item == BottomNavItem.Emergency) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    selectedTextColor = if (item == BottomNavItem.Emergency) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
