package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.FeeOverdueRed
import com.example.ui.theme.FeePaidGreen
import com.example.ui.theme.FeePendingAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPortalScreen(
    students: List<Student>,
    selectedStudent: Student?,
    onSelectStudent: (Student) -> Unit,
    receipts: List<FeeReceipt>,
    messages: List<ParentMessage>,
    attendanceRecords: List<AttendanceRecord>,
    testScores: List<TestScore>
) {
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    val activeStudent = selectedStudent ?: students.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Child Selection Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parent_child_selector_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FamilyRestroom,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Parent Portal View",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (students.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = studentDropdownExpanded,
                            onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = activeStudent?.let { "${it.name} (${it.gradeClass})" } ?: "Select Child",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Viewing Details For Child") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = studentDropdownExpanded,
                                onDismissRequest = { studentDropdownExpanded = false }
                            ) {
                                students.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text("${s.name} • ${s.gradeClass}") },
                                        onClick = {
                                            onSelectStudent(s)
                                            studentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        activeStudent?.let { student ->
            val studentReceipts = receipts.filter { it.studentId == student.id }
            val studentMessages = messages.filter { it.studentId == student.id || it.isBroadcast }
            val studentAttendance = attendanceRecords.filter { it.studentId == student.id }
            val studentScores = testScores.filter { it.studentId == student.id }

            // Fee Status Overview Card
            item {
                val statusEnum = try { FeeStatus.valueOf(student.feeStatus) } catch (e: Exception) { FeeStatus.PENDING }
                val (statusColor, statusBg, statusTitle) = when (statusEnum) {
                    FeeStatus.PAID -> Triple(FeePaidGreen, MaterialTheme.colorScheme.surface, "Fee Status: Paid")
                    FeeStatus.PENDING -> Triple(FeePendingAmber, MaterialTheme.colorScheme.surface, "Fee Status: Pending")
                    FeeStatus.OVERDUE -> Triple(FeeOverdueRed, MaterialTheme.colorScheme.surface, "Fee Status: Overdue")
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = statusBg),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = statusColor)
                                Text(
                                    text = statusTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }

                            AssistChip(
                                onClick = {},
                                label = { Text("Monthly: ₹${student.monthlyFee.toInt()}") }
                            )
                        }

                        Text(
                            text = "Due Date: ${student.dueDate} • Batch: ${student.batchTiming}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Fee Receipts
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Digital Fee Receipts (${studentReceipts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (studentReceipts.isEmpty()) {
                        Text(
                            text = "No fee receipt history found for this month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        studentReceipts.forEach { receipt ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Receipt ${receipt.receiptNo}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${receipt.monthYear} • ${receipt.paymentMode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "₹${receipt.amountPaid.toInt()}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FeePaidGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Teacher Messages / Announcements
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Teacher Messages & Notices (${studentMessages.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (studentMessages.isEmpty()) {
                        Text(
                            text = "No messages sent by teacher yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        studentMessages.forEach { msg ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = msg.subject,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (msg.isBroadcast) {
                                            AssistChip(onClick = {}, label = { Text("Notice", fontSize = 10.sp) })
                                        }
                                    }
                                    Text(
                                        text = msg.messageBody,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Test Performance
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Child's Test Performance (${studentScores.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (studentScores.isEmpty()) {
                        Text(
                            text = "No test scores published yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        studentScores.forEach { score ->
                            val pct = (score.scoreObtained / score.maxScore) * 100
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${score.testTitle} (${score.subject})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Date: ${score.testDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = "${score.scoreObtained.toInt()}/${score.maxScore.toInt()} (${pct.toInt()}%)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
