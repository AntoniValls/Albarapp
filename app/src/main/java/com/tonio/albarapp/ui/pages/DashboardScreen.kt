package com.tonio.albarapp.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tonio.albarapp.User
import com.tonio.albarapp.UserRole
import com.tonio.albarapp.data.WorkSlip
import com.tonio.albarapp.data.WorkSlipStatus
import com.tonio.albarapp.data.WorkSlipRepository
import com.tonio.albarapp.ui.components.WorkSlipCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUser: User,
    onOpenWorkSlip: (WorkSlip) -> Unit = {}
) {
    val userWorkSlips = remember(currentUser) {
        WorkSlipRepository.getForUser(currentUser.id, currentUser.role)
    }

    var search by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTab by remember { mutableStateOf(0) }

    // Tab labels change based on user role
    val tabs = when (currentUser.role) {
        UserRole.SUBCONTRACTOR -> listOf("All", "Draft", "Pending Contractor", "Approved")
        UserRole.CONTRACTOR -> listOf("All", "To Sign", "Pending Manager", "Approved")
        UserRole.MANAGER -> listOf("All", "Pending Approval")
        else -> listOf("All")
    }

    val filtered = remember(userWorkSlips, search, selectedTab, currentUser) {
        val s = search.text.trim().lowercase()
        val base = when (currentUser.role) {
            UserRole.SUBCONTRACTOR -> when (selectedTab) {
                1 -> userWorkSlips.filter { it.status == WorkSlipStatus.DRAFT }
                2 -> userWorkSlips.filter { it.status == WorkSlipStatus.PENDING_CONTRACTOR }
                3 -> userWorkSlips.filter { it.status == WorkSlipStatus.APPROVED }
                else -> userWorkSlips
            }
            UserRole.CONTRACTOR -> when (selectedTab) {
                1 -> userWorkSlips.filter { it.status == WorkSlipStatus.PENDING_CONTRACTOR }
                2 -> userWorkSlips.filter { it.status == WorkSlipStatus.PENDING_MANAGER }
                3 -> userWorkSlips.filter { it.status == WorkSlipStatus.APPROVED }
                else -> userWorkSlips
            }
            UserRole.MANAGER -> when (selectedTab) {
                1 -> userWorkSlips.filter { it.status == WorkSlipStatus.PENDING_MANAGER }
                else -> userWorkSlips
            }
        }

        if (s.isEmpty()) base
        else base.filter {
            it.title.lowercase().contains(s) ||
                    it.location.lowercase().contains(s) ||
                    it.worksiteName.lowercase().contains(s) ||
                    it.contractorName.lowercase().contains(s) ||
                    it.subcontractorName.lowercase().contains(s)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Work Slips",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        // User context card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Viewing as:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        currentUser.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        currentUser.role.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        "${userWorkSlips.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Search field
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search work slips...") },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )

        Spacer(Modifier.height(16.dp))

        // Tabs
        if (tabs.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, label ->
                    val count = when (currentUser.role) {
                        UserRole.SUBCONTRACTOR -> when (index) {
                            1 -> userWorkSlips.count { it.status == WorkSlipStatus.DRAFT }
                            2 -> userWorkSlips.count { it.status == WorkSlipStatus.PENDING_CONTRACTOR }
                            3 -> userWorkSlips.count { it.status == WorkSlipStatus.APPROVED }
                            else -> userWorkSlips.size
                        }
                        UserRole.CONTRACTOR -> when (index) {
                            1 -> userWorkSlips.count { it.status == WorkSlipStatus.PENDING_CONTRACTOR }
                            2 -> userWorkSlips.count { it.status == WorkSlipStatus.PENDING_MANAGER }
                            3 -> userWorkSlips.count { it.status == WorkSlipStatus.APPROVED }
                            else -> userWorkSlips.size
                        }
                        UserRole.MANAGER -> when (index) {
                            1 -> userWorkSlips.count { it.status == WorkSlipStatus.PENDING_MANAGER }
                            else -> userWorkSlips.size
                        }
                    }
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                "$label ($count)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Work slip list
        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(
                        "No work slips found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Try adjusting your filters or search",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    WorkSlipCard(item = item, onViewClick = onOpenWorkSlip)
                }

                // Bottom padding
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}