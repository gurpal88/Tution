package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FeeStatus
import com.example.data.Student
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.SapphirePrimary
import com.example.ui.theme.TuitionManagerTheme
import com.example.ui.viewmodel.TuitionViewModel

enum class NavigationTab(
    val title: String,
    val shortTitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Dashboard", "Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    STUDENTS_FEE("Fees & Roster", "Fees", Icons.Filled.People, Icons.Outlined.People),
    MESSAGES("Messages", "Messages", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
    ATTENDANCE_TASKS("Attendance & Tasks", "Classes", Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment),
    EXPENSES("Expenses", "Ledger", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
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
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
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

    // Handle back press gracefully across perspective & secondary tabs
    BackHandler(enabled = !isTutorView || currentTab != NavigationTab.DASHBOARD) {
        if (!isTutorView) {
            viewModel.togglePerspective()
        } else if (currentTab != NavigationTab.DASHBOARD) {
            currentTab = NavigationTab.DASHBOARD
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(SapphirePrimary, EmeraldAccent)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Tuition Manager",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isTutorView) "Teacher Workspace" else "Parent Portal",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        },
                        actions = {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isTutorView) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.secondaryContainer,
                                border = BorderStroke(
                                    1.dp,
                                    if (isTutorView) MaterialTheme.colorScheme.outlineVariant
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                ),
                                onClick = { viewModel.togglePerspective() },
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .heightIn(min = 40.dp)
                                    .testTag("perspective_toggle")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTutorView) Icons.Outlined.FamilyRestroom else Icons.Outlined.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = if (isTutorView) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isTutorView) "Parent View" else "Teacher View",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTutorView) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                }
            },
            bottomBar = {
                if (isTutorView && !useNavRail) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            modifier = Modifier.testTag("bottom_nav")
                        ) {
                            NavigationTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.shortTitle,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTutorView && useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("bottom_nav")
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.shortTitle) },
                                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                            )
                        }
                    }
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = 840.dp)
                            .fillMaxWidth()
                    ) {
                        AnimatedContent(
                            targetState = Pair(isTutorView, currentTab),
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(220)) + slideInVertically(
                                    animationSpec = tween(220),
                                    initialOffsetY = { it / 24 }
                                )) togetherWith fadeOut(animationSpec = tween(160))
                            },
                            label = "mainScreenTransition"
                        ) { (tutorMode, activeTab) ->
                            if (!tutorMode) {
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
                                when (activeTab) {
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
                                        onSendWhatsApp = { ctx, phone, msg ->
                                            viewModel.openWhatsApp(ctx, phone, msg)
                                        },
                                        onSendSms = { ctx, phone, msg ->
                                            viewModel.openSms(ctx, phone, msg)
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
                                        onSendWhatsApp = { ctx, phone, msg ->
                                            viewModel.openWhatsApp(ctx, phone, msg)
                                        },
                                        onSendSms = { ctx, phone, msg ->
                                            viewModel.openSms(ctx, phone, msg)
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
            }
        }
    }
}
