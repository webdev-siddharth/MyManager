package com.core2studio.mymanager.ui.screens.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.theme.PaleMint
import com.core2studio.mymanager.theme.White
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateOption(val label: String) {
    ALL_TIME("All Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom Range")
}

enum class PriceSort(val label: String) {
    NONE("No Sort"),
    LOW_TO_HIGH("Low to High"),
    HIGH_TO_LOW("High to Low")
}

data class OrderFilterState(
    val status: String = "All",
    val dateOption: DateOption = DateOption.ALL_TIME,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val priceSort: PriceSort = PriceSort.NONE
) {
    val isActive: Boolean
        get() = status != "All" || dateOption != DateOption.ALL_TIME || priceSort != PriceSort.NONE
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderFilterBottomSheet(
    filterState: OrderFilterState,
    onDismiss: () -> Unit,
    onApply: (OrderFilterState) -> Unit,
    onReset: () -> Unit
) {
    var tempStatus by remember { mutableStateOf(filterState.status) }
    var tempDateOption by remember { mutableStateOf(filterState.dateOption) }
    var tempCustomStartDate by remember { mutableStateOf(filterState.customStartDate) }
    var tempCustomEndDate by remember { mutableStateOf(filterState.customEndDate) }
    var tempPriceSort by remember { mutableStateOf(filterState.priceSort) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val statusOptions = listOf("All", "PENDING", "PARTIAL", "COMPLETED")
    val dateOptions = DateOption.entries
    val priceSortOptions = PriceSort.entries

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Filter Orders",
                style = MaterialTheme.typography.titleLarge,
                color = DeepSlate,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Status Section
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleMedium,
                color = DeepSlate,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                statusOptions.forEach { status ->
                    FilterChip(
                        selected = tempStatus == status,
                        onClick = { tempStatus = status },
                        label = { Text(text = status) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = White,
                            containerColor = PaleMint,
                            labelColor = DeepSlate
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = ForestGreen,
                            selectedBorderColor = ForestGreen,
                            enabled = true,
                            selected = tempStatus == status
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Section
            Text(
                text = "Date",
                style = MaterialTheme.typography.titleMedium,
                color = DeepSlate,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                dateOptions.forEach { option ->
                    FilterChip(
                        selected = tempDateOption == option,
                        onClick = {
                            tempDateOption = option
                            if (option != DateOption.CUSTOM) {
                                tempCustomStartDate = null
                                tempCustomEndDate = null
                            }
                        },
                        label = { Text(text = option.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = White,
                            containerColor = PaleMint,
                            labelColor = DeepSlate
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = ForestGreen,
                            selectedBorderColor = ForestGreen,
                            enabled = true,
                            selected = tempDateOption == option
                        )
                    )
                }
            }

            AnimatedVisibility(visible = tempDateOption == DateOption.CUSTOM) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Start Date",
                                style = MaterialTheme.typography.labelMedium,
                                color = DeepSlate.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedButton(
                                onClick = { showStartDatePicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ForestGreen
                                )
                            ) {
                                Text(
                                    text = if (tempCustomStartDate != null) {
                                        dateFormatter.format(Date(tempCustomStartDate!!))
                                    } else {
                                        "Select"
                                    }
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "End Date",
                                style = MaterialTheme.typography.labelMedium,
                                color = DeepSlate.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            OutlinedButton(
                                onClick = { showEndDatePicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ForestGreen
                                )
                            ) {
                                Text(
                                    text = if (tempCustomEndDate != null) {
                                        dateFormatter.format(Date(tempCustomEndDate!!))
                                    } else {
                                        "Select"
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Section
            Text(
                text = "Price",
                style = MaterialTheme.typography.titleMedium,
                color = DeepSlate,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            priceSortOptions.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { tempPriceSort = option }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = tempPriceSort == option,
                        onClick = { tempPriceSort = option },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = ForestGreen,
                            unselectedColor = DeepSlate.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepSlate,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        tempStatus = "All"
                        tempDateOption = DateOption.ALL_TIME
                        tempCustomStartDate = null
                        tempCustomEndDate = null
                        tempPriceSort = PriceSort.NONE
                        onReset()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ForestGreen
                    )
                ) {
                    Text(text = "Reset")
                }
                Button(
                    onClick = {
                        onApply(
                            OrderFilterState(
                                status = tempStatus,
                                dateOption = tempDateOption,
                                customStartDate = tempCustomStartDate,
                                customEndDate = tempCustomEndDate,
                                priceSort = tempPriceSort
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = White
                    )
                ) {
                    Text(text = "Apply")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempCustomStartDate
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        tempCustomStartDate = datePickerState.selectedDateMillis
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK", color = ForestGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel", color = DeepSlate)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempCustomEndDate
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        tempCustomEndDate = datePickerState.selectedDateMillis
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK", color = ForestGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel", color = DeepSlate)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun applyDateFilter(orders: List<com.core2studio.mymanager.data.local.entity.Order>, dateOption: DateOption, customStartDate: Long?, customEndDate: Long?): List<com.core2studio.mymanager.data.local.entity.Order> {
    if (dateOption == DateOption.ALL_TIME) return orders

    val calendar = Calendar.getInstance()
    val now = calendar.timeInMillis

    val (startMillis, endMillis) = when (dateOption) {
        DateOption.TODAY -> {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis to now
        }
        DateOption.THIS_WEEK -> {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis to now
        }
        DateOption.THIS_MONTH -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis to now
        }
        DateOption.THIS_YEAR -> {
            calendar.set(Calendar.MONTH, Calendar.JANUARY)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis to now
        }
        DateOption.CUSTOM -> {
            val start = customStartDate ?: 0L
            val end = customEndDate ?: now
            start to end
        }
    }

    return orders.filter { it.date in startMillis..endMillis }
}

fun applyPriceSort(orders: List<com.core2studio.mymanager.data.local.entity.Order>, priceSort: PriceSort): List<com.core2studio.mymanager.data.local.entity.Order> {
    return when (priceSort) {
        PriceSort.NONE -> orders
        PriceSort.LOW_TO_HIGH -> orders.sortedBy { it.amount }
        PriceSort.HIGH_TO_LOW -> orders.sortedByDescending { it.amount }
    }
}
