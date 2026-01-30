package com.tonio.albarapp.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tonio.albarapp.User
import com.tonio.albarapp.UserRole
import com.tonio.albarapp.MockUsers
import com.tonio.albarapp.data.*
import com.tonio.albarapp.ui.components.DropdownField
import com.tonio.albarapp.ui.components.LineItemForm
import com.tonio.albarapp.ui.components.SignaturePad
import com.tonio.albarapp.ui.components.rememberSignaturePadState
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private fun euros(cents: Long): String =
    NumberFormat.getCurrencyInstance(Locale("es","ES")).format(cents / 100.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewWorkSlipScreen(
    currentUser: User,
    onWorkSlipCreated: () -> Unit = {}
) {
    // Only subcontractors can create new work slips
    if (currentUser.role != UserRole.SUBCONTRACTOR) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Only subcontractors can create work slips",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Current role: ${currentUser.role.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    // stepper: 1=Worksite & Parties, 2=Line Items, 3=Review/Signature
    var step by remember { mutableStateOf(1) }

    // Step 1 state
    var selectedWorksite by remember { mutableStateOf<Worksite?>(null) }
    var workDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedContractor by remember { mutableStateOf<User?>(null) }

    // Get list of contractors (for now from MockUsers, later you can expand this)
    val availableContractors = remember {
        MockUsers.allUsers.filter { it.role == UserRole.CONTRACTOR }
    }

    // Step 2 state
    var lineItems by remember { mutableStateOf(listOf(LineItem())) }
    var vatRate by remember { mutableStateOf(21) }

    val subtotal = lineItems.sumOf { it.lineTotalCents }
    val vatCents = (subtotal * (vatRate / 100.0)).toLong()
    val totalCents = subtotal + vatCents

    // Step 3 state (only subcontractor signature needed)
    val subcontractorSig = rememberSignaturePadState()
    var showSuccessDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Title + step indicator
        Text("New Work Slip", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            when (step) {
                1 -> "Step 1 of 3 • Worksite & Parties"
                2 -> "Step 2 of 3 • Line Items"
                else -> "Step 3 of 3 • Review & Sign"
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        if (step == 1) {
            // ------- STEP 1 -------
            val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

            DropdownField(
                label = "Worksite *",
                options = FakeEntities.worksites,
                selected = selectedWorksite,
                optionLabel = { it.name },
                onSelected = { selectedWorksite = it }
            )

            Spacer(Modifier.height(12.dp))

            var showDatePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = dateFormatter.format(workDate),
                onValueChange = {},
                label = { Text("Work Date *") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) { Text("Pick") }
                }
            )
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = { TextButton(onClick = { showDatePicker = false }) { Text("OK") } }
                ) {
                    val state = rememberDatePickerState(
                        initialSelectedDateMillis = java.util.Date.from(
                            workDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                        ).time
                    )
                    DatePicker(state = state)
                    DisposableEffect(Unit) {
                        onDispose {
                            state.selectedDateMillis?.let { millis ->
                                workDate = java.time.Instant.ofEpochMilli(millis)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            DropdownField(
                label = "Send to Contractor *",
                options = availableContractors,
                selected = selectedContractor,
                optionLabel = { "${it.name} (${it.email})" },
                onSelected = { selectedContractor = it }
            )

            Spacer(Modifier.height(8.dp))

            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        "Your info:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        currentUser.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        currentUser.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onWorkSlipCreated) { Text("Cancel") }
                Button(
                    enabled = selectedWorksite != null && selectedContractor != null,
                    onClick = { step = 2 }
                ) { Text("Next") }
            }
        } else if (step == 2) {
            // ------- STEP 2 -------
            LazyColumn(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(lineItems, key = { idx, _ -> idx }) { index, li ->
                    LineItemForm(
                        item = li,
                        onRemove = if (lineItems.size > 1) {
                            { lineItems = lineItems.toMutableList().also { it.removeAt(index) } }
                        } else null,
                        onChanged = { updated ->
                            lineItems = lineItems.toMutableList().also { it[index] = updated }
                        }
                    )
                }
                item {
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(onClick = { lineItems = lineItems + LineItem() }) {
                        Text("+ Add line")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = vatRate.toString(),
                onValueChange = { v -> vatRate = v.toIntOrNull() ?: vatRate },
                label = { Text("VAT %") },
                modifier = Modifier.width(120.dp)
            )
            Spacer(Modifier.height(8.dp))

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal")
                        Text(euros(subtotal))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("VAT ($vatRate%)")
                        Text(euros(vatCents))
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        Text(euros(totalCents), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { step = 1 }) { Text("Back") }
                Button(
                    enabled = lineItems.any { it.description.isNotBlank() && it.quantity > 0 },
                    onClick = { step = 3 }
                ) { Text("Next") }
            }
        } else {
            // ------- STEP 3: Review & Sign -------
            Column(Modifier.weight(1f, fill = true)) {
                // Summary
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Worksite: ${selectedWorksite?.name ?: "-"}")
                        Text("Date: $workDate")
                        Text("Contractor: ${selectedContractor?.name ?: "-"}")
                        Spacer(Modifier.height(8.dp))
                        Text("Lines: ${lineItems.size}")
                        Text("Total: ${euros(totalCents)}")
                    }
                }

                Spacer(Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "ℹ️ Next Steps",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "1. You sign now as the subcontractor\n" +
                                    "2. Work slip is sent to ${selectedContractor?.name ?: "contractor"}\n" +
                                    "3. Contractor reviews and signs\n" +
                                    "4. Manager approves final work slip",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Only subcontractor signature
                SignaturePad(
                    label = "Your Signature (Subcontractor)",
                    state = subcontractorSig,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { step = 2 }) { Text("Back") }
                Button(
                    enabled = subcontractorSig.hasSignature,
                    onClick = {
                        // Create and save the work slip
                        val newWorkSlip = WorkSlip.create(
                            id = UUID.randomUUID().toString(),
                            title = selectedWorksite?.name ?: "Work Slip",
                            date = workDate,
                            worksiteId = selectedWorksite?.id ?: "",
                            worksiteName = selectedWorksite?.name ?: "",
                            location = selectedWorksite?.let { "${it.address}, ${it.city}" } ?: "",
                            subcontractorId = currentUser.id,
                            subcontractorName = "${currentUser.name} - ${FakeEntities.companies.find { it.id == currentUser.companyId }?.name ?: ""}",
                            contractorId = selectedContractor?.id ?: "",
                            contractorName = "${selectedContractor?.name} - ${FakeEntities.companies.find { it.id == selectedContractor?.companyId }?.name ?: ""}",
                            managerId = MockUsers.manager.id,
                            lineItems = lineItems,
                            vatRate = vatRate,
                            totalCents = totalCents,
                            status = WorkSlipStatus.PENDING_CONTRACTOR,
                            subcontractorSignature = Signature.create(
                                userId = currentUser.id,
                                userName = currentUser.name,
                                timestamp = LocalDateTime.now()
                            ),
                            createdAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now()
                        )

                        WorkSlipRepository.add(newWorkSlip)
                        showSuccessDialog = true
                    }
                ) { Text("Sign & Send") }
            }

            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    confirmButton = {
                        TextButton(onClick = {
                            showSuccessDialog = false
                            onWorkSlipCreated()
                        }) {
                            Text("OK")
                        }
                    },
                    title = { Text("✓ Work Slip Sent!") },
                    text = {
                        Text("Your work slip has been signed and sent to ${selectedContractor?.name} for review and signature.")
                    }
                )
            }
        }
    }
}