package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.TuitionExpense
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MonthSelectorPillBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun ExpensesScreen(
    expenses: List<TuitionExpense>,
    totalRevenue: Double,
    totalExpenses: Double,
    netRevenue: Double,
    selectedMonth: String = "August 2026",
    availableMonths: List<String> = listOf("August 2026", "July 2026", "June 2026", "May 2026", "All Months"),
    onSelectMonth: (String) -> Unit = {},
    onAddExpense: (title: String, category: String, amount: Double, remarks: String) -> Unit,
    onDeleteExpense: (TuitionExpense) -> Unit
) {
    val palette = LocalTuitionStatusPalette.current
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Rent", "Electricity", "Printing", "Stationery", "Salary", "Other")

    val filteredExpenses = if (selectedCategoryFilter == null || selectedCategoryFilter == "All") {
        expenses
    } else {
        expenses.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    val totalVolume = (totalRevenue + totalExpenses).coerceAtLeast(1.0)
    val incomeRatio by animateFloatAsState(
        targetValue = (totalRevenue / totalVolume).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(450),
        label = "incomeRatio"
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddExpenseDialog = true },
                icon = { Icon(Icons.Default.AddCard, contentDescription = null) },
                text = { Text("Record Expense") },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("fab_add_expense")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Selector
            item {
                MonthSelectorPillBar(
                    selectedMonth = selectedMonth,
                    availableMonths = availableMonths,
                    onSelectMonth = onSelectMonth
                )
            }

            // Net Earnings & P&L Hero Card
            item {
                val pnlGradient = if (netRevenue >= 0) {
                    Brush.linearGradient(listOf(SapphireDeep, Color(0xFF115E59)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFF7F1D1D), Color(0xFF991B1B)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(pnlGradient)
                        .drawBehind {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.06f),
                                radius = size.width * 0.34f,
                                center = Offset(size.width * 0.9f, size.height * 0.15f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.04f),
                                radius = size.width * 0.22f,
                                center = Offset(size.width * 0.12f, size.height * 0.92f)
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "NET TUITION EARNINGS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.72f)
                                )
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%.0f", netRevenue)}",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.16f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (netRevenue >= 0) Icons.Default.AccountBalanceWallet else Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Income vs Expenses ratio bar
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { incomeRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = Color(0xFF34D399),
                                trackColor = Color(0xFFFCA5A5).copy(alpha = 0.7f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Income vs Expense Ratio",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${(incomeRatio * 100).toInt()}% Income",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6EE7B7)
                                )
                            }
                        }

                        // Sub-metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFF6EE7B7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Income",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.72f)
                                        )
                                        Text(
                                            text = "₹${String.format(Locale.getDefault(), "%.0f", totalRevenue)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = Color(0xFFFCA5A5),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Expenses",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.72f)
                                        )
                                        Text(
                                            text = "₹${String.format(Locale.getDefault(), "%.0f", totalExpenses)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category Filter Header & Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "Expense Ledger",
                        badgeText = "${filteredExpenses.size}"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            val selected = (selectedCategoryFilter == null && category == "All") || selectedCategoryFilter == category
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedCategoryFilter = if (category == "All") null else category
                                },
                                label = { Text(category) },
                                leadingIcon = if (selected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                shape = RoundedCornerShape(50)
                            )
                        }
                    }
                }
            }

            // Expenses List
            if (filteredExpenses.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        title = "No Expenses Recorded",
                        subtitle = "Tap Record Expense below to log rent, utilities, printing, or staff costs."
                    )
                }
            } else {
                items(filteredExpenses, key = { it.id }) { expense ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = palette.overdueContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (expense.category.lowercase()) {
                                                "rent" -> Icons.Default.HomeWork
                                                "electricity" -> Icons.Default.Bolt
                                                "printing" -> Icons.Default.Print
                                                "stationery" -> Icons.Default.EditNote
                                                "salary" -> Icons.Default.Payments
                                                else -> Icons.Default.Receipt
                                            },
                                            contentDescription = null,
                                            tint = palette.overdue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = expense.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = expense.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = expense.dateString,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (expense.remarks.isNotBlank()) {
                                        Text(
                                            text = expense.remarks,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "-₹${String.format(Locale.getDefault(), "%.0f", expense.amount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.overdue
                                )
                                IconButton(onClick = { onDeleteExpense(expense) }) {
                                    Icon(
                                        Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete Expense",
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onSave = { title, category, amount, remarks ->
                onAddExpense(title, category, amount, remarks)
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, amount: Double, remarks: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Rent") }
    var amountText by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    val categories = listOf("Rent", "Electricity", "Printing", "Stationery", "Salary", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                shape = CircleShape,
                color = LocalTuitionStatusPalette.current.overdueContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.AddCard,
                        contentDescription = null,
                        tint = LocalTuitionStatusPalette.current.overdue
                    )
                }
            }
        },
        title = {
            Text(
                "Record Expense",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title *") },
                    placeholder = { Text("e.g. Electricity Bill") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks / Notes") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onSave(title, category, amt, remarks)
                    }
                },
                enabled = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
