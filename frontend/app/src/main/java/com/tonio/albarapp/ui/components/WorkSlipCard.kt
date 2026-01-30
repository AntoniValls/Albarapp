package com.tonio.albarapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tonio.albarapp.data.WorkSlip
import com.tonio.albarapp.data.WorkSlipStatus
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Business
import java.text.NumberFormat
import java.util.Locale

private fun euros(cents: Long): String =
    NumberFormat.getCurrencyInstance(Locale("es", "ES")).format(cents / 100.0)

@Composable
fun WorkSlipCard(
    item: WorkSlip,
    onViewClick: (WorkSlip) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewClick(item) }
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Title and Status Badge
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(12.dp))
                StatusBadge(item.status)
            }

            Spacer(Modifier.height(12.dp))

            // Amount - Large and prominent
            Text(
                euros(item.totalCents),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            // Details with icons
            DetailRow(
                icon = Icons.Filled.CalendarToday,
                text = item.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            )

            Spacer(Modifier.height(8.dp))

            DetailRow(
                icon = Icons.Filled.LocationOn,
                text = item.location
            )

            Spacer(Modifier.height(8.dp))

            DetailRow(
                icon = Icons.Filled.Business,
                text = item.subcontractorName.split("-").first().trim()
            )

            // Signature status indicators
            if (item.subcontractorSignature != null || item.contractorSignature != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.subcontractorSignature != null) {
                        SignatureIndicator("Subcontractor", true)
                    }
                    if (item.contractorSignature != null) {
                        SignatureIndicator("Contractor", true)
                    }
                    if (item.managerApproval != null) {
                        SignatureIndicator("Manager", true)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SignatureIndicator(label: String, signed: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (signed)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "✓ $label",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = if (signed)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}