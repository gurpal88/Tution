package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FeeReceipt
import com.example.data.FeeStatus
import com.example.data.Student
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsFeeScreen(
    students: List<Student>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterStatus: FeeStatus?,
    onFilterStatusChange: (FeeStatus?) -> Unit,
    receipts: List<FeeReceipt>,
    selectedMonth: String = "August 2026",
    availableMonths: List<String> = listOf("August 2026", "July 2026", "June 2026", "May 2026", "All Months"),
    onSelectMonth: (String) -> Unit = {},
    onAddStudent: (name: String, grade: String, parent: String, phone: String, fee: Double, dueDate: String, batch: String) -> Unit,
    onUpdateFeePayment: (student: Student, amount: Double, monthYear: String, paymentMode: String, remarks: String) -> Unit,
    onDirectStatusUpdate: (student: Student, status: FeeStatus, monthYear: String) -> Unit,
    onSendWhatsApp: (context: Context, phone: String, message: String) -> Unit,
    onSendSms: (context: Context, phone: String, message: String) -> Unit,
    onDeleteStudent: ((Student) -> Unit)? = null,
    onShareReceipt: ((FeeReceipt) -> Unit)? = null,
    initialUpdateStudent: Student? = null
) {
    val context = LocalContext.current
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var selectedStudentForFeeUpdate by remember { mutableStateOf<Student?>(initialUpdateStudent) }
    var showReceiptsLog by remember { mutableStateOf(false) }

    LaunchedEffect(initialUpdateStudent) {
        if (initialUpdateStudent != null) {
            selectedStudentForFeeUpdate = initialUpdateStudent
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddStudentDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Add Student") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_student")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month Filter Selector Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Month:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableMonths) { m ->
                            FilterChip(
                                selected = selectedMonth == m,
                                onClick = { onSelectMonth(m) },
                                label = { Text(m, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search by student name, batch, or parent...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_search_bar"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = filterStatus == null,
                        onClick = { onFilterStatusChange(null) },
                        label = { Text("All Students (${students.size})") },
                        modifier = Modifier.testTag("filter_all")
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == FeeStatus.PAID,
                        onClick = { onFilterStatusChange(FeeStatus.PAID) },
                        label = { Text("Paid") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = FeePaidGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FeePaidContainer,
                            selectedLabelColor = FeePaidGreen
                        ),
                        modifier = Modifier.testTag("filter_paid")
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == FeeStatus.PENDING,
                        onClick = { onFilterStatusChange(FeeStatus.PENDING) },
                        label = { Text("Pending") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = FeePendingAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FeePendingContainer,
                            selectedLabelColor = FeePendingAmber
                        ),
                        modifier = Modifier.testTag("filter_pending")
                    )
                }
                item {
                    FilterChip(
                        selected = filterStatus == FeeStatus.OVERDUE,
                        onClick = { onFilterStatusChange(FeeStatus.OVERDUE) },
                        label = { Text("Overdue") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = FeeOverdueRed,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FeeOverdueContainer,
                            selectedLabelColor = FeeOverdueRed
                        ),
                        modifier = Modifier.testTag("filter_overdue")
                    )
                }
            }

            // Receipts Log Header Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showReceiptsLog) "Payment Receipts Log" else "Student Roster",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { showReceiptsLog = !showReceiptsLog },
                    modifier = Modifier.testTag("toggle_receipts_log")
                ) {
                    Icon(
                        imageVector = if (showReceiptsLog) Icons.Default.People else Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showReceiptsLog) "View Roster" else "View Receipts (${receipts.size})")
                }
            }

            if (showReceiptsLog) {
                // Receipts List View
                if (receipts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No payment receipts found.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(receipts, key = { it.id }) { receipt ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = receipt.studentName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "₹${receipt.amountPaid.toInt()}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = FeePaidGreen
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Receipt: ${receipt.receiptNo} • ${receipt.monthYear}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${receipt.paymentMode} • ${receipt.paymentDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (receipt.remarks.isNotBlank()) {
                                        Text(
                                            text = "Note: ${receipt.remarks}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = { onShareReceipt?.invoke(receipt) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Share,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Share Receipt", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Students List View
                if (students.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "No students match your criteria",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try adjusting your search query or filter chips.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier.testTag("students_list")
                    ) {
                        items(students, key = { it.id }) { student ->
                            StudentFeeCard(
                                student = student,
                                onUpdateFee = { selectedStudentForFeeUpdate = student },
                                onSendReminderWhatsApp = {
                                    val msg = "Dear ${student.parentName}, this is a reminder regarding tuition fee of ₹${student.monthlyFee.toInt()} for ${student.name} (${student.gradeClass}). Due date: ${student.dueDate}."
                                    onSendWhatsApp(context, student.parentPhone, msg)
                                },
                                onSendReminderSms = {
                                    val msg = "Dear ${student.parentName}, tuition fee of ₹${student.monthlyFee.toInt()} is due for ${student.name}. Please pay at your convenience."
                                    onSendSms(context, student.parentPhone, msg)
                                },
                                onToggleStatus = { newStatus ->
                                    onDirectStatusUpdate(student, newStatus, student.lastPaidMonth)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Add New Student
    if (showAddStudentDialog) {
        AddStudentDialog(
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { name, grade, parent, phone, fee, dueDate, batch ->
                onAddStudent(name, grade, parent, phone, fee, dueDate, batch)
                showAddStudentDialog = false
            }
        )
    }

    // Modal Dialog: Update Monthly Fee & Generate Receipt
    selectedStudentForFeeUpdate?.let { student ->
        UpdateFeeDialog(
            student = student,
            onDismiss = { selectedStudentForFeeUpdate = null },
            onConfirm = { amount, monthYear, mode, remarks ->
                onUpdateFeePayment(student, amount, monthYear, mode, remarks)
                selectedStudentForFeeUpdate = null
            }
        )
    }
}

@Composable
fun StudentFeeCard(
    student: Student,
    onUpdateFee: () -> Unit,
    onSendReminderWhatsApp: () -> Unit,
    onSendReminderSms: () -> Unit,
    onToggleStatus: (FeeStatus) -> Unit
) {
    val statusEnum = try {
        FeeStatus.valueOf(student.feeStatus)
    } catch (e: Exception) {
        FeeStatus.PENDING
    }

    val (statusColor, statusBg, statusText) = when (statusEnum) {
        FeeStatus.PAID -> Triple(FeePaidGreen, FeePaidContainer, "PAID")
        FeeStatus.PENDING -> Triple(FeePendingAmber, FeePendingContainer, "PENDING")
        FeeStatus.OVERDUE -> Triple(FeeOverdueRed, FeeOverdueContainer, "OVERDUE")
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Name, Grade & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${student.gradeClass} • ${student.batchTiming}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Parent Contact",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${student.parentName} (${student.parentPhone})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Monthly Fee",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${student.monthlyFee.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "Last Cleared: ${student.lastPaidMonth} • Due Date: ${student.dueDate}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onUpdateFee,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (statusEnum == FeeStatus.PAID) MaterialTheme.colorScheme.secondary else FeePaidGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (statusEnum == FeeStatus.PAID) "Update Fee" else "Mark Paid", fontSize = 12.sp)
                }

                IconButton(
                    onClick = onSendReminderWhatsApp,
                    modifier = Modifier.background(Color(0xFF25D366).copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp Parent",
                        tint = Color(0xFF128C7E)
                    )
                }

                IconButton(
                    onClick = onSendReminderSms,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "SMS Parent",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, grade: String, parent: String, phone: String, fee: Double, dueDate: String, batch: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("Class 10 - Mathematics") }
    var parent by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+91") }
    var feeText by remember { mutableStateOf("1500") }
    var dueDate by remember { mutableStateOf("5th of every month") }
    var batch by remember { mutableStateOf("4:00 PM - 5:30 PM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Student") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Grade / Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = parent,
                    onValueChange = { parent = it },
                    label = { Text("Parent Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Parent Mobile Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Monthly Fee Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    label = { Text("Batch Timing") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && parent.isNotBlank()) {
                        val feeVal = feeText.toDoubleOrNull() ?: 1500.0
                        onConfirm(name, grade, parent, phone, feeVal, dueDate, batch)
                    }
                }
            ) {
                Text("Save Student")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateFeeDialog(
    student: Student,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, monthYear: String, mode: String, remarks: String) -> Unit
) {
    var amountText by remember { mutableStateOf(student.monthlyFee.toInt().toString()) }
    var monthYear by remember { mutableStateOf("August 2026") }
    var paymentMode by remember { mutableStateOf("UPI") }
    var remarks by remember { mutableStateOf("Fee paid on time") }

    val paymentModes = listOf("UPI", "Cash", "Bank Transfer", "Cheque")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Fee Payment for ${student.name}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Student: ${student.name} (${student.gradeClass})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("Billing Month & Year") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Paid (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Payment Mode:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(paymentModes) { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode) }
                        )
                    }
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks / Receipt Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: student.monthlyFee
                    onConfirm(amount, monthYear, paymentMode, remarks)
                }
            ) {
                Text("Generate Receipt & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
