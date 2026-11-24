package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Quiz
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.LocalTuitionStatusPalette
import com.example.ui.theme.SapphireDeep

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
    val palette = LocalTuitionStatusPalette.current
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    val activeStudent = selectedStudent ?: students.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Parent Portal Gradient Header + Child Selector
        item {
            val portalGradient = Brush.linearGradient(
                colors = listOf(Color(0xFF0F4C5C), SapphireDeep)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(portalGradient)
                    .drawBehind {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.07f),
                            radius = size.width * 0.35f,
                            center = Offset(size.width * 0.92f, size.height * 0.14f)
                        )
                    }
                    .testTag("parent_child_selector_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FamilyRestroom,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Parent Portal",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Track fees, attendance, notices & test progress",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }

                    if (students.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = studentDropdownExpanded,
                            onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = activeStudent?.let { "${it.name} • ${it.gradeClass}" } ?: "Select Child",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Viewing Student", color = Color.White.copy(alpha = 0.8f)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White.copy(alpha = 0.7f),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                                    focusedTrailingIconColor = Color.White,
                                    unfocusedTrailingIconColor = Color.White
                                ),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = studentDropdownExpanded,
                                onDismissRequest = { studentDropdownExpanded = false }
                            ) {
                                students.forEach { s ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                StudentAvatarBadge(name = s.name, size = 30.dp)
                                                Column {
                                                    Text(s.name, fontWeight = FontWeight.SemiBold)
                                                    Text(
                                                        s.gradeClass,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        },
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

        if (activeStudent == null) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.School,
                    title = "No Student Profiles Found",
                    subtitle = "Switch to Teacher Mode to enroll students first."
                )
            }
        } else {
            val student = activeStudent
            val studentReceipts = receipts.filter { it.studentId == student.id }
            val studentMessages = messages.filter { it.studentId == student.id || it.isBroadcast }
            val studentAttendance = attendanceRecords.filter { it.studentId == student.id }
            val studentScores = testScores.filter { it.studentId == student.id }

            val statusEnum = try {
                FeeStatus.valueOf(student.feeStatus)
            } catch (e: Exception) {
                FeeStatus.PENDING
            }
            val todayAttendance = studentAttendance.firstOrNull()?.status ?: "Not Marked"

            // Student Quick Snapshot Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                StudentAvatarBadge(name = student.name, size = 48.dp)
                                Column {
                                    Text(
                                        text = student.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = student.batchTiming,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            StatusPillBadge(status = statusEnum)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Monthly Fee",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${student.monthlyFee.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Due ${student.dueDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Attendance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = todayAttendance,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = when (todayAttendance) {
                                            AttendanceStatus.PRESENT.name -> palette.paid
                                            AttendanceStatus.ABSENT.name -> palette.overdue
                                            AttendanceStatus.LATE.name -> palette.pending
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    Text(
                                        text = "Selected date",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Digital Fee Receipts
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "Digital Fee Receipts",
                        badgeText = "${studentReceipts.size}"
                    )

                    if (studentReceipts.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            title = "No Receipts for This Period",
                            subtitle = "Receipts appear here once fee payments are recorded by the teacher."
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            studentReceipts.forEach { receipt ->
                                TicketReceiptCard(receipt = receipt)
                            }
                        }
                    }
                }
            }

            // Teacher Messages & Notices
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "Teacher Notices",
                        badgeText = "${studentMessages.size}"
                    )

                    if (studentMessages.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Outlined.Campaign,
                            title = "No Notices Yet",
                            subtitle = "Announcements and direct teacher updates will show up here."
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            studentMessages.forEach { msg ->
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = msg.subject,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (msg.isBroadcast) {
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                                ) {
                                                    Text(
                                                        text = "Broadcast",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.tertiary,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
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
            }

            // Child's Test Performance
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = "Test Performance",
                        badgeText = "${studentScores.size}"
                    )

                    if (studentScores.isEmpty()) {
                        EmptyStateCard(
                            icon = Icons.Outlined.Quiz,
                            title = "No Test Scores Published",
                            subtitle = "Assessment results and grades will appear here once graded."
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            studentScores.forEach { score ->
                                val pct = if (score.maxScore > 0) (score.scoreObtained / score.maxScore) * 100 else 0.0
                                val (gradeText, gradeColor) = when {
                                    pct >= 90 -> "A+" to palette.paid
                                    pct >= 80 -> "A" to palette.paid
                                    pct >= 70 -> "B" to palette.pending
                                    pct >= 60 -> "C" to palette.pending
                                    else -> "D" to palette.overdue
                                }

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
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            CircularScoreBadge(
                                                percentage = pct.toFloat(),
                                                color = gradeColor,
                                                gradeText = gradeText
                                            )
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

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
