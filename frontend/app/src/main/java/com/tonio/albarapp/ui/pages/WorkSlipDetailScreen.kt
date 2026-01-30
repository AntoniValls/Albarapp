package com.tonio.albarapp.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.widget.Toast
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Send
import com.tonio.albarapp.data.Comment
import com.tonio.albarapp.data.PdfGenerator
import com.tonio.albarapp.User
import com.tonio.albarapp.UserRole
import com.tonio.albarapp.data.*
import com.tonio.albarapp.ui.components.SignaturePad
import com.tonio.albarapp.ui.components.StatusBadge
import com.tonio.albarapp.ui.components.rememberSignaturePadState
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private fun euros(cents: Long): String =
    NumberFormat.getCurrencyInstance(Locale("es","ES")).format(cents / 100.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkSlipDetailScreen(
    workSlipId: String,
    currentUser: User,
    onBack: () -> Unit
) {
    val workSlip = remember(workSlipId) { WorkSlipRepository.getById(workSlipId) }
    val context = LocalContext.current
    var isGeneratingPdf by remember { mutableStateOf(false) }

    if (workSlip == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Work slip not found", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    var showSignDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showApproveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work Slip Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                workSlip.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(workSlip.status)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            euros(workSlip.totalCents),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Work Details
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Work Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        DetailRow("Date", workSlip.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        DetailRow("Worksite", workSlip.worksiteName)
                        DetailRow("Location", workSlip.location)
                    }
                }
            }

            // Parties
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Parties", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        DetailRow("Subcontractor", workSlip.subcontractorName)
                        DetailRow("Contractor", workSlip.contractorName)
                    }
                }
            }

            // Line Items
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Line Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            items(workSlip.lineItems) { item ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.description, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${item.quantity} ${item.unit} × ${euros(item.unitPriceCents)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                euros(item.lineTotalCents),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Total
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal")
                            Text(euros(workSlip.lineItems.sumOf { it.lineTotalCents }))
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("VAT (${workSlip.vatRate}%)")
                            Text(euros((workSlip.lineItems.sumOf { it.lineTotalCents } * workSlip.vatRate / 100.0).toLong()))
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(euros(workSlip.totalCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Generate PDF button (always visible)
            item {
                Button(
                    onClick = {
                        isGeneratingPdf = true
                        try {
                            val pdfFile = PdfGenerator.generateWorkSlipPdf(context, workSlip)

                            // Open the PDF
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                pdfFile
                            )

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                            }

                            context.startActivity(Intent.createChooser(intent, "Open PDF"))

                            Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isGeneratingPdf = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGeneratingPdf
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isGeneratingPdf) "Generating PDF..." else "Generate PDF")
                }

                Spacer(Modifier.height(16.dp))
            }

            // Signatures
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text("Signatures", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))

                        // Subcontractor signature
                        SignatureStatus(
                            label = "Subcontractor",
                            signature = workSlip.subcontractorSignature
                        )

                        Spacer(Modifier.height(8.dp))

                        // Contractor signature
                        SignatureStatus(
                            label = "Contractor",
                            signature = workSlip.contractorSignature
                        )

                        Spacer(Modifier.height(8.dp))

                        // Manager approval
                        if (workSlip.status == WorkSlipStatus.PENDING_MANAGER ||
                            workSlip.status == WorkSlipStatus.APPROVED ||
                            workSlip.status == WorkSlipStatus.REJECTED) {
                            SignatureStatus(
                                label = "Manager Approval",
                                signature = workSlip.managerApproval
                            )
                        }
                    }
                }
            }

            // Comments Section
            item {
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Comments (${workSlip.comments.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                Icons.Filled.Comment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (workSlip.comments.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No comments yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Spacer(Modifier.height(12.dp))
                            workSlip.comments.forEach { comment ->
                                CommentItem(comment)
                                if (comment != workSlip.comments.last()) {
                                    Spacer(Modifier.height(12.dp))
                                    HorizontalDivider()
                                    Spacer(Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Add Comment Section
            item {
                var commentText by remember { mutableStateOf("") }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Add Comment",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Write a comment...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    val comment = Comment.create(
                                        id = UUID.randomUUID().toString(),
                                        workSlipId = workSlip.id,
                                        userId = currentUser.id,
                                        userName = currentUser.name,
                                        userRole = currentUser.role,
                                        message = commentText.trim(),
                                        timestamp = LocalDateTime.now()
                                    )
                                    WorkSlipRepository.addComment(workSlip.id, comment)
                                    commentText = ""
                                    onBack() // Refresh by going back
                                }
                            },
                            enabled = commentText.isNotBlank(),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Post Comment")
                        }
                    }
                }
            }

            // Action buttons
            item {
                when {
                    // Contractor needs to sign
                    currentUser.role == UserRole.CONTRACTOR &&
                            workSlip.status == WorkSlipStatus.PENDING_CONTRACTOR &&
                            workSlip.contractorId == currentUser.id -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showSignDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Sign Work Slip")
                            }
                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Cancel, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Reject")
                            }
                        }
                    }

                    // Manager needs to approve
                    currentUser.role == UserRole.MANAGER &&
                            workSlip.status == WorkSlipStatus.PENDING_MANAGER &&
                            workSlip.managerId == currentUser.id -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showApproveDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Approve Work Slip")
                            }
                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Cancel, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Reject")
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Sign Dialog
    if (showSignDialog) {
        ContractorSignDialog(
            workSlip = workSlip,
            currentUser = currentUser,
            onDismiss = { showSignDialog = false },
            onSigned = {
                showSignDialog = false
                onBack()
            }
        )
    }

    // Approve Dialog
    if (showApproveDialog) {
        ManagerApprovalDialog(
            workSlip = workSlip,
            currentUser = currentUser,
            onDismiss = { showApproveDialog = false },
            onApproved = {
                showApproveDialog = false
                onBack()
            }
        )
    }

// Reject Dialog
    if (showRejectDialog) {
        var rejectionReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Work Slip") },
            text = {
                Column {
                    Text("Please provide a reason for rejection:")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        placeholder = { Text("Enter rejection reason...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Add rejection comment
                        if (rejectionReason.isNotBlank()) {
                            val comment = Comment.create(
                                id = UUID.randomUUID().toString(),
                                workSlipId = workSlip.id,
                                userId = currentUser.id,
                                userName = currentUser.name,
                                userRole = currentUser.role,
                                message = rejectionReason.trim(),
                                timestamp = LocalDateTime.now(),
                                isRejectionReason = true
                            )
                            WorkSlipRepository.addComment(workSlip.id, comment)
                        }

                        // Update status
                        val updated = workSlip.copy(
                            status = WorkSlipStatus.REJECTED,
                            updatedAt = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        )
                        WorkSlipRepository.update(updated)
                        showRejectDialog = false
                        onBack()
                    },
                    enabled = rejectionReason.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SignatureStatus(label: String, signature: Signature?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            if (signature != null) {
                Text(
                    signature.userName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    signature.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (signature != null) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Signed",
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                "Pending",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ContractorSignDialog(
    workSlip: WorkSlip,
    currentUser: User,
    onDismiss: () -> Unit,
    onSigned: () -> Unit
) {
    val signatureState = rememberSignaturePadState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign Work Slip") },
        text = {
            Column {
                Text("Please sign below to approve this work slip and send it to the manager.")
                Spacer(Modifier.height(16.dp))
                SignaturePad(
                    label = "Your Signature",
                    state = signatureState,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = workSlip.copy(
                        status = WorkSlipStatus.PENDING_MANAGER,
                        contractorSignature = Signature.create(
                            userId = currentUser.id,
                            userName = currentUser.name,
                            timestamp = LocalDateTime.now()
                        ),
                        updatedAt = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    )
                    WorkSlipRepository.update(updated)
                    onSigned()
                },
                enabled = signatureState.hasSignature
            ) {
                Text("Sign & Send to Manager")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ManagerApprovalDialog(
    workSlip: WorkSlip,
    currentUser: User,
    onDismiss: () -> Unit,
    onApproved: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Approve Work Slip") },
        text = { Text("Are you sure you want to approve this work slip? This will mark it as completed.") },
        confirmButton = {
            Button(
                onClick = {
                    val updated = workSlip.copy(
                        status = WorkSlipStatus.APPROVED,
                        managerApproval = Signature.create(
                            userId = currentUser.id,
                            userName = currentUser.name,
                            timestamp = LocalDateTime.now()
                        ),
                        updatedAt = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    )
                    WorkSlipRepository.update(updated)  // This line was missing!
                    onApproved()  // This line was missing!
                }
            ) {
                Text("Approve")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CommentItem(comment: Comment) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        comment.userName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (comment.userRole) {
                                com.tonio.albarapp.UserRole.SUBCONTRACTOR -> MaterialTheme.colorScheme.tertiaryContainer
                                com.tonio.albarapp.UserRole.CONTRACTOR -> MaterialTheme.colorScheme.secondaryContainer
                                com.tonio.albarapp.UserRole.MANAGER -> MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                    ) {
                        Text(
                            comment.userRole.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    comment.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (comment.isRejectionReason) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Rejection",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            comment.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}