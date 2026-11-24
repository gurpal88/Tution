package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import com.example.data.FeeReceipt
import com.example.data.FeeStatus
import com.example.data.Student
import com.example.ui.components.*
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
    val palette = LocalTuitionStatusPalette.current
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var selectedStudentForFeeUpdate by remember { mutableStateOf<Student?>(initialUpdateStudent) }
    var showReceiptsLog by remember { mutableStateOf(false) }

    LaunchedEffect(initialUpdateStudent) {
        if (initialUpdateStudent != null) {
            selectedStudentForFeeUpdate = initialUpdateStudent
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (!showReceiptsLog) {
                ExtendedFloatingActionButton(
                    onClick = { showAddStudentDialog = true },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text("Add Student") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("fab_add_student")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Month Selector
            MonthSelectorPillBar(
                selectedMonth = selectedMonth,
                availableMonths = availableMonths,
                onSelectMonth = onSelectMonth
            )

            // Search Bar (Pill Shaped)
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        "Search students, batch, or parent...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_search_bar")
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
                        label = { Text("All (${students.size})") },
                        shape = RoundedCornerShape(50),
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
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = palette.paid,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.paidContainer,
                            selectedLabelColor = palette.paid
                        ),
                        shape = RoundedCornerShape(50),
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
                                tint = palette.pending,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.pendingContainer,
                            selectedLabelColor = palette.pending
                        ),
                        shape = RoundedCornerShape(50),
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
                                Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = palette.overdue,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.overdueContainer,
                            selectedLabelColor = palette.overdue
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("filter_overdue")
                    )
                }
            }

            // Segmented Header Toggle (Roster vs Receipts Log)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showReceiptsLog) "Payment Receipts" else "Student Roster",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                FilledTonalButton(
                    onClick = { showReceiptsLog = !showReceiptsLog },
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("toggle_receipts_log")
                ) {
                    Icon(
                        imageVector = if (showReceiptsLog) Icons.Default.People else Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showReceiptsLog) "Roster (${students.size})" else "Receipts (${receipts.size})",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            AnimatedContent(
                targetState = showReceiptsLog,
                label = "rosterReceiptsSwitch",
                modifier = Modifier.weight(1f)
            ) { isReceipts ->
                if (isReceipts) {
                    if (receipts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateCard(
                                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                title = "No Receipts for $selectedMonth",
                                subtitle = "Once you mark a student's fee as paid, digital receipts appear here ready to share."
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 84.dp)
                        ) {
                            items(receipts, key = { it.id }) { receipt ->
                                TicketReceiptCard(
                                    receipt = receipt,
                                    onShare = { onShareReceipt?.invoke(receipt) }
                                )
                            }
                        }
                    }
                } else {
                    if (students.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateCard(
                                icon = Icons.Default.PersonSearch,
                                title = "No Matching Students",
                                subtitle = "Try clearing your search text or selecting a different fee status filter."
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 88.dp),
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
                                    onDeleteStudent = onDeleteStudent?.let { deleteFn ->
                                        { deleteFn(student) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddStudentDialog) {
        AddStudentDialog(
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { name, grade, parent, phone, fee, dueDate, batch ->
                onAddStudent(name, grade, parent, phone, fee, dueDate, batch)
                showAddStudentDialog = false
            }
        )
    }

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
    onDeleteStudent: (() -> Unit)? = null
) {
    val palette = LocalTuitionStatusPalette.current
    val statusEnum = try {
        FeeStatus.valueOf(student.feeStatus)
    } catch (e: Exception) {
        FeeStatus.PENDING
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Avatar, Name, Grade/Batch, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudentAvatarBadge(name = student.name, size = 46.dp)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${student.gradeClass} • ${student.batchTiming}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusPillBadge(status = statusEnum)
            }

            // Info Well
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Parent: ${student.parentName}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${student.parentPhone} • Due ${student.dueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${student.monthlyFee.toInt()}/mo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Paid: ${student.lastPaidMonth}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onUpdateFee,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (statusEnum == FeeStatus.PAID) MaterialTheme.colorScheme.secondary else palette.paid,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (statusEnum == FeeStatus.PAID) "Update Fee" else "Mark Paid",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                IconButton(
                    onClick = onSendReminderWhatsApp,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WhatsAppTint.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "WhatsApp Parent",
                        tint = WhatsAppGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onSendReminderSms,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "SMS Parent",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (onDeleteStudent != null) {
                    IconButton(
                        onClick = onDeleteStudent,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.overdueContainer.copy(alpha = 0.75f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Remove Student",
                            tint = palette.overdue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

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
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        title = {
            Text(
                "Enroll New Student",
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
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name *") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Grade / Subject") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = parent,
                    onValueChange = { parent = it },
                    label = { Text("Parent Name *") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Parent Mobile Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Monthly Fee (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    label = { Text("Batch Timing") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Monthly Due Date") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
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
                },
                enabled = name.isNotBlank() && parent.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
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
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                shape = CircleShape,
                color = LocalTuitionStatusPalette.current.paidContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        tint = LocalTuitionStatusPalette.current.paid
                    )
                }
            }
        },
        title = {
            Text(
                "Record Fee Payment",
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
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudentAvatarBadge(name = student.name, size = 36.dp)
                        Column {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = student.gradeClass,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("Billing Month & Year") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Paid (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Payment Mode",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(paymentModes) { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks / Receipt Note") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: student.monthlyFee
                    onConfirm(amount, monthYear, paymentMode, remarks)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Generate Receipt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
