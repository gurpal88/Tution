package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FeeStatus
import com.example.data.Student
import com.example.ui.screens.*
import com.example.ui.theme.TuitionManagerTheme
import com.example.ui.viewmodel.TuitionViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Outlined.Dashboard),
    STUDENTS_FEE("Fees & Roster", Icons.Outlined.People),
    MESSAGES("Messages", Icons.Outlined.Chat),
    ATTENDANCE_TASKS("Attendance & Tasks", Icons.Outlined.Assignment),
    EXPENSES("Expenses", Icons.Outlined.AccountBalanceWallet)
}

class MainActivity : ComponentActivity() {

    private val viewModel: TuitionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TuitionManagerTheme {
                TuitionAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuitionAppContent(viewModel: TuitionViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var initialFeeFilter by remember { mutableStateOf<FeeStatus?>(null) }
    var initialFeeStudentUpdate by remember { mutableStateOf<Student?>(null) }

    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val filteredStudents by viewModel.filteredStudents.collectAsStateWithLifecycle()
    val searchQuery by viewModel.studentSearchQuery.collectAsStateWithLifecycle()
    val feeFilter by viewModel.feeFilterStatus.collectAsStateWithLifecycle()
    val dashboardStats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val receipts by viewModel.filteredReceipts.collectAsStateWithLifecycle()
    val messages by viewModel.allMessages.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val testScores by viewModel.allTestScores.collectAsStateWithLifecycle()
    val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedAttendanceDate.collectAsStateWithLifecycle()
    val attendanceRecords by viewModel.attendanceForSelectedDate.collectAsStateWithLifecycle()

    val isTutorView by viewModel.isTutorView.collectAsStateWithLifecycle()
    val selectedParentStudent by viewModel.selectedParentStudent.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Tuition Manager",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isTutorView) "Teacher / Tutor Dashboard" else "Parent View Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Switch perspective (Teacher / Parent)
                    FilterChip(
                        selected = !isTutorView,
                        onClick = { viewModel.togglePerspective() },
                        label = { Text(if (isTutorView) "Parent Mode" else "Teacher Mode") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isTutorView) Icons.Outlined.FamilyRestroom else Icons.Outlined.AdminPanelSettings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("perspective_toggle")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (isTutorView) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("bottom_nav")
                ) {
                    NavigationTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isTutorView) {
                ParentPortalScreen(
                    students = allStudents,
                    selectedStudent = selectedParentStudent,
                    onSelectStudent = { viewModel.selectParentStudent(it) },
                    receipts = receipts,
                    messages = messages,
                    attendanceRecords = attendanceRecords,
                    testScores = testScores
                )
            } else {
                when (currentTab) {
                    NavigationTab.DASHBOARD -> DashboardScreen(
                        stats = dashboardStats,
                        students = allStudents,
                        receipts = receipts,
                        selectedMonth = selectedMonth,
                        availableMonths = viewModel.availableMonths,
                        onSelectMonth = { viewModel.setSelectedMonth(it) },
                        onNavigateToFees = { status ->
                            viewModel.setFeeFilterStatus(status)
                            currentTab = NavigationTab.STUDENTS_FEE
                        },
                        onNavigateToMessages = { currentTab = NavigationTab.MESSAGES },
                        onNavigateToAttendance = { currentTab = NavigationTab.ATTENDANCE_TASKS },
                        onNavigateToTasks = { currentTab = NavigationTab.ATTENDANCE_TASKS },
                        onQuickAddStudent = { currentTab = NavigationTab.STUDENTS_FEE },
                        onQuickUpdateFee = { student ->
                            initialFeeStudentUpdate = student
                            currentTab = NavigationTab.STUDENTS_FEE
                        }
                    )

                    NavigationTab.STUDENTS_FEE -> StudentsFeeScreen(
                        students = filteredStudents,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setStudentSearchQuery(it) },
                        filterStatus = feeFilter,
                        onFilterStatusChange = { viewModel.setFeeFilterStatus(it) },
                        receipts = receipts,
                        selectedMonth = selectedMonth,
                        availableMonths = viewModel.availableMonths,
                        onSelectMonth = { viewModel.setSelectedMonth(it) },
                        onAddStudent = { name, grade, parent, phone, fee, dueDate, batch ->
                            viewModel.addStudent(name, grade, parent, phone, fee, dueDate, batch)
                        },
                        onUpdateFeePayment = { student, amount, monthYear, mode, remarks ->
                            viewModel.updateFeePayment(student, amount, monthYear, mode, remarks)
                            initialFeeStudentUpdate = null
                        },
                        onDirectStatusUpdate = { student, status, monthYear ->
                            viewModel.updateFeeStatusDirect(student, status, monthYear)
                        },
                        onSendWhatsApp = { context, phone, msg ->
                            viewModel.openWhatsApp(context, phone, msg)
                        },
                        onSendSms = { context, phone, msg ->
                            viewModel.openSms(context, phone, msg)
                        },
                        onDeleteStudent = { student ->
                            viewModel.deleteStudent(student)
                        },
                        onShareReceipt = { receipt ->
                            viewModel.shareReceipt(context, receipt)
                        },
                        initialUpdateStudent = initialFeeStudentUpdate
                    )

                    NavigationTab.MESSAGES -> MessageCenterScreen(
                        students = allStudents,
                        messages = messages,
                        onSendMessage = { student, subject, body, category, isBroadcast ->
                            viewModel.sendParentMessage(student, subject, body, category, isBroadcast)
                        },
                        onSendWhatsApp = { context, phone, msg ->
                            viewModel.openWhatsApp(context, phone, msg)
                        },
                        onSendSms = { context, phone, msg ->
                            viewModel.openSms(context, phone, msg)
                        }
                    )

                    NavigationTab.ATTENDANCE_TASKS -> AttendanceTaskScreen(
                        students = allStudents,
                        selectedDate = selectedDate,
                        onDateSelected = { viewModel.setSelectedAttendanceDate(it) },
                        attendanceRecords = attendanceRecords,
                        onSaveAttendanceRecord = { student, status, remarks ->
                            viewModel.saveAttendanceRecord(student, status, remarks)
                        },
                        onMarkAllPresent = { viewModel.markAllPresentForToday() },
                        tasks = tasks,
                        onAddTask = { title, desc, subject, batch, dueDate, category ->
                            viewModel.addTask(title, desc, subject, batch, dueDate, category)
                        },
                        onToggleTask = { taskId, isCompleted ->
                            viewModel.toggleTask(taskId, isCompleted)
                        },
                        onDeleteTask = { task ->
                            viewModel.deleteTask(task)
                        },
                        testScores = testScores,
                        onAddTestScore = { student, title, subject, score, maxScore, remarks ->
                            viewModel.addTestScore(student, title, subject, score, maxScore, remarks)
                        }
                    )

                    NavigationTab.EXPENSES -> ExpensesScreen(
                        expenses = expenses,
                        totalRevenue = dashboardStats.totalCollectedThisMonth,
                        totalExpenses = dashboardStats.totalExpensesThisMonth,
                        netRevenue = dashboardStats.netRevenueThisMonth,
                        selectedMonth = selectedMonth,
                        availableMonths = viewModel.availableMonths,
                        onSelectMonth = { viewModel.setSelectedMonth(it) },
                        onAddExpense = { title, category, amount, remarks ->
                            viewModel.addExpense(title, category, amount, remarks)
                        },
                        onDeleteExpense = { expense ->
                            viewModel.deleteExpense(expense)
                        }
                    )
                }
            }
        }
    }
}
