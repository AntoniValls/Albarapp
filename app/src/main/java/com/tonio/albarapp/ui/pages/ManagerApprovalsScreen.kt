package com.tonio.albarapp.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ManagerApprovalsScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Aprobaciones (Manager)", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Here we will show pending approvals.")
    }
}