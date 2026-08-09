package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    onAddTestScore: (student: Student, title: String, subject: String, score: Double, maxScore: Double, remarks: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Attendance, 1 = Homework/Tasks, 2 = Test Marks
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddTestScoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    icon = { Icon(Icons.Default.AddTask, contentDescription = null) },
                    text = { Text("Add Task / Homework") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_task")
                )
            } else if (selectedTab == 2) {
                ExtendedFloatingActionButton(
                    onClick = { showAddTestScoreDialog = true },
                    icon = { Icon(Icons.Default.Grade, contentDescription = null) },
                    text = { Text("Enter Test Score") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tab Header
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Attendance") },
                    icon = { Icon(Icons.Outlined.HowToReg, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Homework (${tasks.count { !it.isCompleted }})") },
                    icon = { Icon(Icons.Outlined.Assignment, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Test Marks") },
                    icon = { Icon(Icons.Outlined.Quiz, contentDescription = null) }
                )
            }

            when (selectedTab) {
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
                2 -> TestMarksTabContent(
                    testScores = testScores
                )
            }
        }
    }

    // Modal Dialog: Add Task
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, subject, batch, dueDate, category ->
                onAddTask(title, desc, subject, batch, dueDate, category)
                showAddTaskDialog = false
            }
        )
    }

    // Modal Dialog: Enter Test Score
    if (showAddTestScoreDialog) {
        AddTestScoreDialog(
            students = students,
            onDismiss = { showAddTestScoreDialog = false },
            onConfirm = { student, title, subject, score, maxScore, remarks ->
                onAddTestScore(student, title, subject, score, maxScore, remarks)
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
    val attendanceMap = remember(attendanceRecords) {
        attendanceRecords.associateBy { it.studentId }
    }

    val presentCount = attendanceRecords.count { it.status == AttendanceStatus.PRESENT.name }
    val absentCount = attendanceRecords.count { it.status == AttendanceStatus.ABSENT.name }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Date Selector & Summary Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
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
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Date: $selectedDate",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onMarkAllPresent,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Mark All Present", fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("Present: $presentCount", color = FeePaidGreen, fontWeight = FontWeight.Bold)
                        Text("Absent: $absentCount", color = FeeOverdueRed, fontWeight = FontWeight.Bold)
                        Text("Total: ${students.size}", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        items(students, key = { it.id }) { student ->
            val record = attendanceMap[student.id]
            val currentStatus = record?.status ?: AttendanceStatus.PRESENT.name

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = currentStatus == AttendanceStatus.PRESENT.name,
                            onClick = { onSaveRecord(student, AttendanceStatus.PRESENT, "") },
                            label = { Text("P") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FeePaidGreen,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.width(44.dp)
                        )
                        FilterChip(
                            selected = currentStatus == AttendanceStatus.ABSENT.name,
                            onClick = { onSaveRecord(student, AttendanceStatus.ABSENT, "") },
                            label = { Text("A") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FeeOverdueRed,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.width(44.dp)
                        )
                        FilterChip(
                            selected = currentStatus == AttendanceStatus.LATE.name,
                            onClick = { onSaveRecord(student, AttendanceStatus.LATE, "") },
                            label = { Text("L") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FeePendingAmber,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.width(44.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TasksTabContent(
    tasks: List<TeacherTask>,
    onToggleTask: (taskId: Int, isCompleted: Boolean) -> Unit,
    onDeleteTask: (TeacherTask) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No teacher tasks or homework assignments added yet.")
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task.id, it) }
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                AssistChip(
                                    onClick = {},
                                    label = { Text(task.category, fontSize = 10.sp) },
                                    modifier = Modifier.height(22.dp)
                                )
                            }
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Subject: ${task.subject} • Batch: ${task.batch} • Due: ${task.dueDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { onDeleteTask(task) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Task", tint = FeeOverdueRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestMarksTabContent(testScores: List<TestScore>) {
    if (testScores.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No test scores recorded yet.")
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(testScores, key = { it.id }) { score ->
                val percentage = (score.scoreObtained / score.maxScore) * 100
                val (gradeText, gradeColor) = when {
                    percentage >= 90 -> "A+" to FeePaidGreen
                    percentage >= 80 -> "A" to FeePaidGreen
                    percentage >= 70 -> "B" to FeePendingAmber
                    percentage >= 60 -> "C" to FeePendingAmber
                    else -> "D" to FeeOverdueRed
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = score.studentName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${score.testTitle} • ${score.subject}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Date: ${score.testDate} ${if (score.remarks.isNotBlank()) "• Note: " + score.remarks else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${score.scoreObtained.toInt()} / ${score.maxScore.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = gradeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Grade $gradeText (${percentage.toInt()}%)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = gradeColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Homework or Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Task Instructions / Details") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = batch,
                    onValueChange = { batch = it },
                    label = { Text("Target Batch") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date") },
                    singleLine = true,
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
                }
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
    onConfirm: (student: Student, title: String, subject: String, score: Double, maxScore: Double, remarks: String) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var testTitle by remember { mutableStateOf("Unit Test 1") }
    var subject by remember { mutableStateOf("Science") }
    var scoreText by remember { mutableStateOf("45") }
    var maxScoreText by remember { mutableStateOf("50") }
    var remarks by remember { mutableStateOf("Good attempt") }
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Student Test Score") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
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
                        modifier = Modifier.menuAnchor().fillMaxWidth()
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

                OutlinedTextField(
                    value = testTitle,
                    onValueChange = { testTitle = it },
                    label = { Text("Test Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = scoreText,
                        onValueChange = { scoreText = it },
                        label = { Text("Score") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxScoreText,
                        onValueChange = { maxScoreText = it },
                        label = { Text("Max Score") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedStudent?.let { student ->
                        val scoreVal = scoreText.toDoubleOrNull() ?: 0.0
                        val maxScoreVal = maxScoreText.toDoubleOrNull() ?: 100.0
                        onConfirm(student, testTitle, subject, scoreVal, maxScoreVal, remarks)
                    }
                }
            ) {
                Text("Save Test Score")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
