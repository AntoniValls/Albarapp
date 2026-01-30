package com.tonio.albarapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import com.tonio.albarapp.User
import com.tonio.albarapp.UserRole
import com.tonio.albarapp.ui.navigation.DrawerItem
import com.tonio.albarapp.ui.navigation.baseDrawerItems
import com.tonio.albarapp.ui.navigation.managerDrawerItems
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.tonio.albarapp.R

@Composable
fun DrawerContent(
    currentRoute: String,
    user: User?,
    onItemClick: (DrawerItem) -> Unit,
    onSwitchUser: (() -> Unit)? = null
) {
    ModalDrawerSheet {
        Column(Modifier.fillMaxSize()) {
            // Header with logo and app name
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Icon(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Albarapp Logo",
                    modifier = Modifier.size(80.dp),
                    tint = Color.Unspecified
                )

                Spacer(Modifier.height(8.dp))

                // App name
                Text(
                    "Albarapp",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "Civil Works Management",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Display current user info
                if (user != null) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = user.role.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Navigation items
            Column(Modifier.weight(1f)) {
                baseDrawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = { onItemClick(item) },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                // Show manager section only for managers
                if (user?.role == UserRole.MANAGER) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Gestión",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    managerDrawerItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = { onItemClick(item) },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }

            // User switcher button at the bottom (for testing)
            if (onSwitchUser != null) {
                HorizontalDivider()
                TextButton(
                    onClick = onSwitchUser,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = "Switch user")
                    Spacer(Modifier.width(8.dp))
                    Text("Switch User (Testing)")
                }
            }
        }
    }
}