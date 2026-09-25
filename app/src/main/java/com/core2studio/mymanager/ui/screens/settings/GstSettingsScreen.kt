package com.core2studio.mymanager.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core2studio.mymanager.ui.components.MyManagerTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstSettingsScreen(
    gstPricingMode: String,
    gstRate: Int,
    gstType: String,
    hasGstin: Boolean,
    gstEnabled: Boolean = false,
    onSave: (gstEnabled: Boolean, pricingMode: String, rate: Int, type: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(gstEnabled) }
    var pricingMode by remember { mutableStateOf(gstPricingMode) }
    var rate by remember { mutableIntStateOf(gstRate) }
    var type by remember { mutableStateOf(gstType) }
    var rateExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MyManagerTopBar(
                title = "GST Settings",
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Master toggle
            SettingsCard(title = "Enable GST") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Apply GST to orders and invoices",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.material3.Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                }
            }

            if (!hasGstin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "GSTIN Required",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "GST settings are only available after adding a GSTIN in Business Info.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (enabled) {
            // Section 1: GST Pricing Mode
            SettingsCard(title = "GST Pricing Mode") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Inclusive: Price shown includes GST. Exclusive: GST added on top.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = pricingMode == "INCLUSIVE",
                            onClick = { pricingMode = "INCLUSIVE" },
                            label = { Text("Tax Inclusive") },
                            leadingIcon = { if (pricingMode == "INCLUSIVE") Icon(androidx.compose.material.icons.Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.onPrimary) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        FilterChip(
                            selected = pricingMode == "EXCLUSIVE",
                            onClick = { pricingMode = "EXCLUSIVE" },
                            label = { Text("Tax Exclusive") },
                            leadingIcon = { if (pricingMode == "EXCLUSIVE") Icon(androidx.compose.material.icons.Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.onPrimary) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Section 2: GST Rate
            SettingsCard(title = "GST Rate") {
                ExposedDropdownMenuBox(
                    expanded = rateExpanded,
                    onExpandedChange = { rateExpanded = !rateExpanded }
                ) {
                    OutlinedTextField(
                        value = "${rate}%",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select GST Rate") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rateExpanded) }
                    )
                    DropdownMenu(
                        expanded = rateExpanded,
                        onDismissRequest = { rateExpanded = false }
                    ) {
                        val gstRateOptions = listOf(
                            Triple(0, "Essential items", ""),
                            Triple(3, "Gold & Silver", ""),
                            Triple(5, "Daily necessities", ""),
                            Triple(18, "electronics, services, and most items", ""),
                            Triple(40, "Luxury & sin goods", "")
                        )
                        gstRateOptions.forEach { (r, desc, _) ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${r}%")
                                        Text(
                                            desc,
                                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = { rate = r; rateExpanded = false }
                            )
                        }
                    }
                }
            }

            // Section 3: GST Type
            SettingsCard(title = "GST Type") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "CGST+SGST for intra-state sales. IGST for inter-state sales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = type == "CGST_SGST",
                            onClick = { type = "CGST_SGST" },
                            label = { Text("CGST + SGST (Intra-state)") },
                            leadingIcon = { if (type == "CGST_SGST") Icon(androidx.compose.material.icons.Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.onPrimary) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        FilterChip(
                            selected = type == "IGST",
                            onClick = { type = "IGST" },
                            label = { Text("IGST (Inter-state)") },
                            leadingIcon = { if (type == "IGST") Icon(androidx.compose.material.icons.Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.onPrimary) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = { onSave(enabled, pricingMode, rate, type) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Save GST Settings", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit
) {
    androidx.compose.material3.DropdownMenuItem(
        onClick = onClick,
        text = text
    )
}