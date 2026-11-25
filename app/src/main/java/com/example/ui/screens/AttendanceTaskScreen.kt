package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Assignment
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.components.CircularScoreBadge
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StudentAvatarBadge
import com.example.ui.theme.LocalTuitionStatusPalette
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceTaskScreen(
    students: List<Student>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    attendanceRecords: List<AttendanceRecord>,
    onSaveAttendanceRecord: (student: Student, status: AttendanceStatus, remarks: String) -> Unit,
    onMarkAllPresent: () -> Unit,
    tasks: List<TeacherTask>,
    onAddTask: (title: String, desc: String, subject: String, batch: String, dueDate: String, category: String) -> Unit,
    onToggleTask: (taskId: Int, isCompleted: Boolean) -> Unit,
    onDeleteTask: (TeacherTask) -> Unit,
    testScores: List<TestScore>,
    onAddTestScore: (student: Student, title: String, subject: String, score: Double, maxScore: Double, remarks: String, notifyParent: Boolean) -> Unit,
    onSendMarksPortal: (score: TestScore, student: Student?) -> Unit,
    onSendMarksWhatsApp: (context: Context, phone: String, message: String) -> Unit,
    onSendMarksSms: (context: Context, phone: String, message: String) -> Unit,
    onShareMarks: (context: Context, score: TestScore, student: Student?) -> Unit,
    formatMarksMessage: (score: TestScore, student: Student?) -> String,
    initialSubTab: Int = 0
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(initialSubTab) } // 0 = Attendance, 1 = Tasks, 2 = Marks
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddTestScoreDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialSubTab) {
        selectedTab = initialSubTab
    }

    val openTasksCount = tasks.count { !it.isCompleted }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    icon = { Icon(Icons.Default.AddTask, contentDescription = null) },
                    text = { Text("Add Task") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("fab_add_task")
                )
            } else if (selectedTab == 2) {
                ExtendedFloatingActionButton(
                    onClick = { showAddTestScoreDialog = true },
                    icon = { Icon(Icons.Default.Grade, contentDescription = null) },
                    text = { Text("Record Marks") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("fab_add_test_score")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Minimal Pill Segmented Switcher
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabItems = listOf(
                        Triple(0, "Attendance", Icons.Outlined.HowToReg),
                        Triple(1, "Tasks ($openTasksCount)", Icons.AutoMirrored.Outlined.Assignment),
                        Triple(2, "Marks (${testScores.size})", Icons.Outlined.Quiz)
                    )
                    tabItems.forEach { (idx, label, icon) ->
                        val selected = selectedTab == idx
                        val bgColor by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            animationSpec = tween(180),
                            label = "tabBg"
                        )
                        val fgColor by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(180),
                            label = "tabFg"
                        )
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = bgColor,
                            shadowElevation = if (selected) 1.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedTab = idx }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = fgColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = fgColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                label = "attendanceTabSwitch",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    0 -> AttendanceTabContent(
                        students = students,
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected,
                        attendanceRecords = attendanceRecords,
                        onSaveRecord = onSaveAttendanceRecord,
                        onMarkAllPresent = onMarkAllPresent
                    )
                    1 -> TasksTabContent(
                        tasks = tasks,
                        onToggleTask = onToggleTask,
                        onDeleteTask = onDeleteTask
                    )
                    else -> TestMarksTabContent(
                        students = students,
                        testScores = testScores,
                        onSendPortal = { score, student -> onSendMarksPortal(score, student) },
                        onSendWhatsApp = { score, student ->
                            if (student != null && student.parentPhone.isNotBlank()) {
                                onSendMarksWhatsApp(context, student.parentPhone, formatMarksMessage(score, student))
                            } else {
                                onShareMarks(context, score, student)
                            }
                        },
                        onSendSms = { score, student ->
                            if (student != null && student.parentPhone.isNotBlank()) {
                                onSendMarksSms(context, student.parentPhone, formatMarksMessage(score, student))
                            } else {
                                onShareMarks(context, score, student)
                            }
                        },
                        onShare = { score, student ->
                            onShareMarks(context, score, student)
                        }
                    )
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, subject, batch, dueDate, category ->
                onAddTask(title, desc, subject, batch, dueDate, category)
                showAddTaskDialog = false
            }
        )
    }

    if (showAddTestScoreDialog) {
        AddTestScoreDialog(
            students = students,
            onDismiss = { showAddTestScoreDialog = false },
            onConfirm = { student, title, subject, score, maxScore, remarks, notifyParent ->
                onAddTestScore(student, title, subject, score, maxScore, remarks, notifyParent)
                showAddTestScoreDialog = false
            }
        )
    }
}

@Composable
fun AttendanceTabContent(
    students: List<Student>,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    attendanceRecords: List<AttendanceRecord>,
    onSaveRecord: (student: Student, status: AttendanceStatus, remarks: String) -> Unit,
    onMarkAllPresent: () -> Unit
) {
    val palette = LocalTuitionStatusPalette.current
    val attendanceMap = remember(attendanceRecords) {
        attendanceRecords.associateBy { it.studentId }
    }

    val presentCount = attendanceRecords.count { it.status == AttendanceStatus.PRESENT.name }
    val absentCount = attendanceRecords.count { it.status == AttendanceStatus.ABSENT.name }
    val lateCount = attendanceRecords.count { it.status == AttendanceStatus.LATE.name }

    val presentRatio by animateFloatAsState(
        targetValue = if (students.isNotEmpty()) presentCount.toFloat() / students.size.toFloat() else 0f,
        animationSpec = tween(350),
        label = "attProgress"
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 84.dp)
    ) {
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Roll Call",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onMarkAllPresent,
                            enabled = students.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.heightIn(min = 42.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("All Present", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    LinearProgressIndicator(
                        progress = { presentRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = palette.paid,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AttendanceStatPill(
                            label = "Present",
                            value = "$presentCount",
                            fg = palette.paid,
                            bg = palette.paidContainer,
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceStatPill(
                            label = "Absent",
                            value = "$absentCount",
                            fg = palette.overdue,
                            bg = palette.overdueContainer,
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceStatPill(
                            label = "Late",
                            value = "$lateCount",
                            fg = palette.pending,
                            bg = palette.pendingContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (students.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.HowToReg,
                    title = "No Students Enrolled",
                    subtitle = "Add students in the Fees & Roster tab to track daily attendance."
                )
            }
        } else {
            items(students, key = { it.id }) { student ->
                val record = attendanceMap[student.id]
                val currentStatus = record?.status ?: AttendanceStatus.PRESENT.name

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
                            StudentAvatarBadge(name = student.name, size = 42.dp)
                            Column {
                                Text(
                                    text = student.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = student.gradeClass,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AttendanceTogglePill(
                                letter = "P",
                                selected = currentStatus == AttendanceStatus.PRESENT.name,
                                activeColor = palette.paid,
                                onClick = { onSaveRecord(student, AttendanceStatus.PRESENT, "") }
                            )
                            AttendanceTogglePill(
                                letter = "A",
                                selected = currentStatus == AttendanceStatus.ABSENT.name,
                                activeColor = palette.overdue,
                                onClick = { onSaveRecord(student, AttendanceStatus.ABSENT, "") }
                            )
                            AttendanceTogglePill(
                                letter = "L",
                                selected = currentStatus == AttendanceStatus.LATE.name,
                                activeColor = palette.pending,
                                onClick = { onSaveRecord(student, AttendanceStatus.LATE, "") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceStatPill(
    label: String,
    value: String,
    fg: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$value ",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = fg
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = fg
            )
        }
    }
}

@Composable
private fun AttendanceTogglePill(
    letter: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TasksTabContent(
    tasks: List<TeacherTask>,
    onToggleTask: (taskId: Int, isCompleted: Boolean) -> Unit,
    onDeleteTask: (TeacherTask) -> Unit
) {
    val palette = LocalTuitionStatusPalette.current
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateCard(
                icon = Icons.AutoMirrored.Outlined.Assignment,
                title = "No Homework or Tasks",
                subtitle = "Tap Add Task below to assign homework or schedule class prep."
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task.id, it) }
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = task.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (task.description.isNotBlank()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${task.subject} • ${task.batch}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = "Due: ${task.dueDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteTask(task) }) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete Task",
                                tint = palette.overdue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestMarksTabContent(
    students: List<Student>,
    testScores: List<TestScore>,
    onSendPortal: (TestScore, Student?) -> Unit,
    onSendWhatsApp: (TestScore, Student?) -> Unit,
    onSendSms: (TestScore, Student?) -> Unit,
    onShare: (TestScore, Student?) -> Unit
) {
    val palette = LocalTuitionStatusPalette.current
    val studentLookup = remember(students) { students.associateBy { it.id } }

    if (testScores.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateCard(
                icon = Icons.Outlined.Quiz,
                title = "No Marks Recorded Yet",
                subtitle = "Tap Record Marks below to enter test scores and send them directly to parents."
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            items(testScores, key = { it.id }) { score ->
                val student = studentLookup[score.studentId]
                val percentage = if (score.maxScore > 0) (score.scoreObtained / score.maxScore) * 100 else 0.0
                val (gradeText, gradeColor) = when {
                    percentage >= 90 -> "A+" to palette.paid
                    percentage >= 80 -> "A" to palette.paid
                    percentage >= 70 -> "B" to palette.pending
                    percentage >= 60 -> "C" to palette.pending
                    else -> "D" to palette.overdue
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                CircularScoreBadge(
                                    percentage = percentage.toFloat(),
                                    color = gradeColor,
                                    gradeText = gradeText
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = score.studentName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${score.testTitle} • ${score.subject}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${score.testDate}" + if (score.remarks.isNotBlank()) " • ${score.remarks}" else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${score.scoreObtained.toInt()}/${score.maxScore.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${percentage.toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = gradeColor
                                )
                            }
                        }

                        // Send Marks Action Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = { onSendPortal(score, student) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 40.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send to Portal", style = MaterialTheme.typography.labelMedium)
                            }

                            IconButton(
                                onClick = { onSendWhatsApp(score, student) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(WhatsAppTint.copy(alpha = 0.14f))
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "Send Marks via WhatsApp",
                                    tint = WhatsAppGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onSendSms(score, student) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                            ) {
                                Icon(
                                    Icons.Default.Sms,
                                    contentDescription = "Send Marks via SMS",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onShare(score, student) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share Marks",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, desc: String, subject: String, batch: String, dueDate: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }
    var batch by remember { mutableStateOf("Class 10") }
    var dueDate by remember { mutableStateOf("Tomorrow") }
    var category by remember { mutableStateOf("HOMEWORK") }

    val taskCategories = listOf("HOMEWORK", "CLASS_PREP", "TEST_PREP")

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
                    Icon(Icons.Default.AddTask, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        title = {
            Text(
                "Create Homework / Task",
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(taskCategories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.replace("_", " ")) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Instructions / Details") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    label = { Text("Target Batch") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, desc, subject, batch, dueDate, category)
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTestScoreDialog(
    students: List<Student>,
    onDismiss: () -> Unit,
    onConfirm: (student: Student, title: String, subject: String, score: Double, maxScore: Double, remarks: String, notifyParent: Boolean) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var testTitle by remember { mutableStateOf("Unit Test 1") }
    var subject by remember { mutableStateOf("Science") }
    var scoreText by remember { mutableStateOf("45") }
    var maxScoreText by remember { mutableStateOf("50") }
    var remarks by remember { mutableStateOf("Good attempt") }
    var notifyParent by remember { mutableStateOf(true) }
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Grade, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
            }
        },
        title = {
            Text(
                "Record Student Marks",
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
                if (students.isEmpty()) {
                    Text(
                        text = "Please enroll a student first in Fees & Roster before recording test marks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = studentDropdownExpanded,
                        onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedStudent?.name ?: "Select Student",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Student") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = studentDropdownExpanded,
                            onDismissRequest = { studentDropdownExpanded = false }
                        ) {
                            students.forEach { student ->
                                DropdownMenuItem(
                                    text = { Text("${student.name} (${student.gradeClass})") },
                                    onClick = {
                                        selectedStudent = student
                                        studentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = testTitle,
                    onValueChange = { testTitle = it },
                    label = { Text("Test Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = scoreText,
                        onValueChange = { scoreText = it },
                        label = { Text("Score") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxScoreText,
                        onValueChange = { maxScoreText = it },
                        label = { Text("Max") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Send to Parent Portal",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = notifyParent,
                        onCheckedChange = { notifyParent = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedStudent?.let { student ->
                        val scoreVal = scoreText.toDoubleOrNull() ?: 0.0
                        val maxScoreVal = maxScoreText.toDoubleOrNull() ?: 100.0
                        onConfirm(student, testTitle, subject, scoreVal, maxScoreVal, remarks, notifyParent)
                    }
                },
                enabled = selectedStudent != null && testTitle.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (notifyParent) "Save & Send" else "Save Marks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
