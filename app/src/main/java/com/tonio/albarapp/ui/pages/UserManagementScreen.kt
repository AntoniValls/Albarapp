package com.tonio.albarapp.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tonio.albarapp.User
import com.tonio.albarapp.UserRole
import com.tonio.albarapp.data.UserRepository
import com.tonio.albarapp.data.FakeEntities
import java.util.UUID

@Composable
fun UserManagementScreen() {
    var users by remember { mutableStateOf(UserRepository.getAll()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var selectedRoleFilter by remember { mutableStateOf<UserRole?>(null) }

    // Filter users by role
    val filteredUsers = if (selectedRoleFilter != null) {
        users.filter { it.role == selectedRoleFilter }
    } else {
        users
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manage Users",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add User")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserRoleCard(
                role = UserRole.SUBCONTRACTOR,
                count = users.count { it.role == UserRole.SUBCONTRACTOR },
                selected = selectedRoleFilter == UserRole.SUBCONTRACTOR,
                onClick = {
                    selectedRoleFilter = if (selectedRoleFilter == UserRole.SUBCONTRACTOR) null else UserRole.SUBCONTRACTOR
                },
                modifier = Modifier.weight(1f)
            )
            UserRoleCard(
                role = UserRole.CONTRACTOR,
                count = users.count { it.role == UserRole.CONTRACTOR },
                selected = selectedRoleFilter == UserRole.CONTRACTOR,
                onClick = {
                    selectedRoleFilter = if (selectedRoleFilter == UserRole.CONTRACTOR) null else UserRole.CONTRACTOR
                },
                modifier = Modifier.weight(1f)
            )
            UserRoleCard(
                role = UserRole.MANAGER,
                count = users.count { it.role == UserRole.MANAGER },
                selected = selectedRoleFilter == UserRole.MANAGER,
                onClick = {
                    selectedRoleFilter = if (selectedRoleFilter == UserRole.MANAGER) null else UserRole.MANAGER
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // User list
        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No users found",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredUsers, key = { it.id }) { user ->
                    UserCard(
                        user = user,
                        onEdit = { userToEdit = user },
                        onDelete = { userToDelete = user }
                    )
                }
            }
        }
    }

    // Add/Edit User Dialog
    if (showAddDialog || userToEdit != null) {
        AddEditUserDialog(
            user = userToEdit,
            onDismiss = {
                showAddDialog = false
                userToEdit = null
            },
            onSave = { user ->
                if (userToEdit != null) {
                    UserRepository.update(user)
                } else {
                    UserRepository.add(user)
                }
                users = UserRepository.getAll()
                showAddDialog = false
                userToEdit = null
            }
        )
    }

    // Delete confirmation dialog
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete ${userToDelete?.name}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        UserRepository.delete(userToDelete!!.id)
                        users = UserRepository.getAll()
                        userToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserRoleCard(
    role: UserRole,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.primary
            )
            Text(
                text = role.name.lowercase().replaceFirstChar { it.uppercase() } + "s",
                style = MaterialTheme.typography.bodySmall,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = user.role.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditUserDialog(
    user: User?,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var selectedRole by remember { mutableStateOf(user?.role ?: UserRole.SUBCONTRACTOR) }
    var selectedCompany by remember { mutableStateOf(
        FakeEntities.companies.find { it.id == user?.companyId }
    ) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Add New User" else "Edit User") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Role selection
                Text("Role *", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    UserRole.values().forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = {
                                Text(
                                    role.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                // Company selection
                Text("Company *", style = MaterialTheme.typography.labelMedium)
                FakeEntities.companies.forEach { company ->
                    FilterChip(
                        selected = selectedCompany?.id == company.id,
                        onClick = { selectedCompany = company },
                        label = { Text(company.name, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && selectedCompany != null) {
                        val newUser = User(
                            id = user?.id ?: UUID.randomUUID().toString(),
                            name = name,
                            email = email,
                            role = selectedRole,
                            companyId = selectedCompany?.id
                        )
                        onSave(newUser)
                    }
                },
                enabled = name.isNotBlank() && email.isNotBlank() && selectedCompany != null
            ) {
                Text(if (user == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}