package com.tonio.albarapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tonio.albarapp.data.LineItem
import java.text.NumberFormat
import java.util.Locale

private fun euros(cents: Long): String =
    NumberFormat.getCurrencyInstance(Locale("es","ES")).format(cents / 100.0)

@Composable
fun LineItemForm(
    item: LineItem,
    onRemove: (() -> Unit)? = null,
    onChanged: (LineItem) -> Unit
) {
    var description by remember { mutableStateOf(item.description) }
    var quantityText by remember { mutableStateOf(item.quantity.toString()) }
    var unit by remember { mutableStateOf(item.unit) }
    var unitPriceText by remember { mutableStateOf("%.2f".format(item.unitPriceCents / 100.0)) }

    fun propagate() {
        val qty = quantityText.toDoubleOrNull() ?: 0.0
        val priceCents = ((unitPriceText.replace(",", ".").toDoubleOrNull() ?: 0.0) * 100).toLong()
        onChanged(
            item.copy(
                description = description,
                quantity = qty,
                unit = unit,
                unitPriceCents = priceCents
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it; propagate() },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it; propagate() },
                    label = { Text("Qty") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Unit (simple text for now; can be a dropdown later)
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it; propagate() },
                    label = { Text("Unit") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = unitPriceText,
                    onValueChange = { unitPriceText = it; propagate() },
                    label = { Text("Unit Price (€)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.weight(1.5f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Line total: " + euros(
                        ((unitPriceText.replace(",", ".").toDoubleOrNull() ?: 0.0) * 100).toLong()
                            .times(quantityText.toDoubleOrNull() ?: 0.0).toLong()
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (onRemove != null) {
                    TextButton(onClick = onRemove) { Text("Remove") }
                }
            }
        }
    }
}
