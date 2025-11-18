package com.tonio.albarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import com.tonio.albarapp.data.WorkSlipRepository
import com.tonio.albarapp.data.UserRepository
import com.tonio.albarapp.ui.components.DrawerContent
import com.tonio.albarapp.ui.navigation.Routes
import com.tonio.albarapp.ui.navigation.navigateToWorkSlipDetail
import com.tonio.albarapp.ui.pages.DashboardScreen
import com.tonio.albarapp.ui.pages.ManagerApprovalsScreen
import com.tonio.albarapp.ui.pages.NewWorkSlipScreen
import com.tonio.albarapp.ui.pages.WorkSlipDetailScreen
import com.tonio.albarapp.ui.pages.UserManagementScreen
import com.tonio.albarapp.ui.theme.AlbarappTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Clear old data (temporary - remove after first run)
        //getSharedPreferences("workslip_prefs", Context.MODE_PRIVATE)
        //    .edit()
        //    .clear()
        //   .apply()

        // Initialize repositories with persistent storage
        WorkSlipRepository.initialize(this)
        UserRepository.initialize(this)

        setContent {
            AlbarappTheme {
                AlbaranApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbaranApp() {
    // Start with contractor user, allow switching for testing
    var user by remember { mutableStateOf(MockUsers.contractor) }
    var showUserDialog by remember { mutableStateOf(false) }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // for highlighting the current item
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Routes.DASHBOARD

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentRoute = currentRoute,
                user = user,
                onItemClick = { item ->
                    scope.launch { drawerState.close() }
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        popUpTo(Routes.DASHBOARD)
                    }
                },
                onSwitchUser = {
                    scope.launch { drawerState.close() }
                    showUserDialog = true
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text("Albarapp")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "menu")
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.DASHBOARD,
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        currentUser = user,
                        onOpenWorkSlip = { workSlip ->
                            navController.navigate(navigateToWorkSlipDetail(workSlip.id))
                        }
                    )
                }

                composable(Routes.NEW_WORKSLIP) {
                    NewWorkSlipScreen(
                        currentUser = user,
                        onWorkSlipCreated = {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.DASHBOARD) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.MANAGER_APPROVALS) {
                    ManagerApprovalsScreen()
                }

                composable(
                    route = Routes.WORKSLIP_DETAIL,
                    arguments = listOf(navArgument("workSlipId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val workSlipId = backStackEntry.arguments?.getString("workSlipId") ?: ""
                    WorkSlipDetailScreen(
                        workSlipId = workSlipId,
                        currentUser = user,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.USER_MANAGEMENT) {
                    UserManagementScreen()
                }
            }
        }
    }

    // User selection dialog
    if (showUserDialog) {
        UserSelectionDialog(
            onUserSelected = { selectedUser ->
                user = selectedUser
                showUserDialog = false
            },
            onDismiss = { showUserDialog = false }
        )
    }
}

@Composable
fun UserSelectionDialog(
    onUserSelected: (User) -> Unit,
    onDismiss: () -> Unit
) {
    val allUsers = remember { UserRepository.getAll() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Switch User (Testing Mode)") },
        text = {
            Column {
                Text("Select a user to test different roles:")
                Spacer(Modifier.height(16.dp))

                if (allUsers.isEmpty()) {
                    Text(
                        "No users available. Add users in Manage Users.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    allUsers.forEach { testUser ->
                        TextButton(
                            onClick = { onUserSelected(testUser) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = androidx.compose.ui.Alignment.Start
                            ) {
                                Text(
                                    testUser.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    "${testUser.role.name.lowercase().replaceFirstChar { it.uppercase() }} - ${testUser.email}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (testUser != allUsers.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}