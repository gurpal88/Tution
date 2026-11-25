package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardStats(
    val totalStudents: Int = 0,
    val paidCount: Int = 0,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0,
    val totalCollectedThisMonth: Double = 0.0,
    val totalExpensesThisMonth: Double = 0.0,
    val netRevenueThisMonth: Double = 0.0,
    val pendingAmountTotal: Double = 0.0,
    val pendingTasksCount: Int = 0
)

class TuitionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TuitionDatabase.getDatabase(application, viewModelScope)
    private val repository = TuitionRepository(db.tuitionDao())

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReceipts: StateFlow<List<FeeReceipt>> = repository.allReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<ParentMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TeacherTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTestScores: StateFlow<List<TestScore>> = repository.allTestScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<TuitionExpense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month Filter State
    private val _selectedMonth = MutableStateFlow("August 2026")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    val availableMonths = listOf("August 2026", "July 2026", "June 2026", "May 2026", "April 2026", "All Months")

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    private fun monthYearToYearMonth(monthYear: String): String {
        return try {
            val sdfIn = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val date = sdfIn.parse(monthYear)
            if (date != null) {
                SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date)
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    val filteredReceipts: StateFlow<List<FeeReceipt>> = combine(
        allReceipts,
        _selectedMonth
    ) { receipts, month ->
        if (month == "All Months") {
            receipts
        } else {
            val ym = monthYearToYearMonth(month)
            receipts.filter { receipt ->
                receipt.monthYear.equals(month, ignoreCase = true) ||
                        (ym.isNotEmpty() && receipt.paymentDate.startsWith(ym))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExpenses: StateFlow<List<TuitionExpense>> = combine(
        allExpenses,
        _selectedMonth
    ) { expenses, month ->
        if (month == "All Months") {
            expenses
        } else {
            val ym = monthYearToYearMonth(month)
            expenses.filter { expense ->
                (ym.isNotEmpty() && expense.dateString.startsWith(ym)) ||
                        expense.title.contains(month, ignoreCase = true) ||
                        expense.remarks.contains(month, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search & Filter State
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    private val _feeFilterStatus = MutableStateFlow<FeeStatus?>(null) // null = ALL
    val feeFilterStatus: StateFlow<FeeStatus?> = _feeFilterStatus.asStateFlow()

    val filteredStudents: StateFlow<List<Student>> = combine(
        allStudents,
        _studentSearchQuery,
        _feeFilterStatus
    ) { students, query, statusFilter ->
        students.filter { student ->
            val matchesQuery = student.name.contains(query, ignoreCase = true) ||
                    student.gradeClass.contains(query, ignoreCase = true) ||
                    student.parentName.contains(query, ignoreCase = true)
            val matchesStatus = statusFilter == null || student.feeStatus == statusFilter.name
            matchesQuery && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics Calculation
    val dashboardStats: StateFlow<DashboardStats> = combine(
        allStudents,
        filteredReceipts,
        allTasks,
        filteredExpenses,
        _selectedMonth
    ) { students, receipts, tasks, expenses, month ->
        val paid = students.count { it.feeStatus == FeeStatus.PAID.name }
        val pending = students.count { it.feeStatus == FeeStatus.PENDING.name }
        val overdue = students.count { it.feeStatus == FeeStatus.OVERDUE.name }

        val collectedMonth = receipts.sumOf { it.amountPaid }
        val expensesTotal = expenses.sumOf { it.amount }
        val netRevenue = collectedMonth - expensesTotal

        val pendingTotal = students
            .filter { it.feeStatus == FeeStatus.PENDING.name || it.feeStatus == FeeStatus.OVERDUE.name }
            .sumOf { it.monthlyFee }

        val pendingTasks = tasks.count { !it.isCompleted }

        DashboardStats(
            totalStudents = students.size,
            paidCount = paid,
            pendingCount = pending,
            overdueCount = overdue,
            totalCollectedThisMonth = collectedMonth,
            totalExpensesThisMonth = expensesTotal,
            netRevenueThisMonth = netRevenue,
            pendingAmountTotal = pendingTotal,
            pendingTasksCount = pendingTasks
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Attendance Date State
    private val _selectedAttendanceDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedAttendanceDate: StateFlow<String> = _selectedAttendanceDate.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val attendanceForSelectedDate: StateFlow<List<AttendanceRecord>> = _selectedAttendanceDate
        .flatMapLatest { dateStr -> repository.getAttendanceForDate(dateStr) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Perspective (Tutor vs Parent)
    private val _isTutorView = MutableStateFlow(true)
    val isTutorView: StateFlow<Boolean> = _isTutorView.asStateFlow()

    private val _selectedParentStudent = MutableStateFlow<Student?>(null)
    val selectedParentStudent: StateFlow<Student?> = _selectedParentStudent.asStateFlow()

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun setFeeFilterStatus(status: FeeStatus?) {
        _feeFilterStatus.value = status
    }

    fun togglePerspective() {
        _isTutorView.value = !_isTutorView.value
    }

    fun selectParentStudent(student: Student?) {
        _selectedParentStudent.value = student
    }

    fun setSelectedAttendanceDate(dateStr: String) {
        _selectedAttendanceDate.value = dateStr
    }

    // --- Student & Fee Actions ---
    fun addStudent(
        name: String,
        gradeClass: String,
        parentName: String,
        parentPhone: String,
        monthlyFee: Double,
        dueDate: String,
        batchTiming: String
    ) {
        viewModelScope.launch {
            val newStudent = Student(
                name = name,
                gradeClass = gradeClass,
                parentName = parentName,
                parentPhone = parentPhone,
                monthlyFee = monthlyFee,
                feeStatus = FeeStatus.PENDING.name,
                dueDate = dueDate,
                batchTiming = batchTiming,
                joiningDate = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
            )
            repository.insertStudent(newStudent)
        }
    }

    fun updateFeePayment(
        student: Student,
        amountPaid: Double,
        monthYear: String,
        paymentMode: String,
        remarks: String
    ) {
        viewModelScope.launch {
            repository.markFeeAsPaid(
                studentId = student.id,
                studentName = student.name,
                amount = amountPaid,
                monthYear = monthYear,
                paymentMode = paymentMode,
                remarks = remarks
            )
        }
    }

    fun updateFeeStatusDirect(student: Student, status: FeeStatus, monthYear: String) {
        viewModelScope.launch {
            repository.updateStudentFeeStatusDirectly(student.id, status.name, monthYear)
        }
    }

    // --- Parent Messaging ---
    fun sendParentMessage(
        student: Student?,
        subject: String,
        body: String,
        category: MessageCategory,
        isBroadcast: Boolean
    ) {
        viewModelScope.launch {
            val message = ParentMessage(
                studentId = if (isBroadcast) null else student?.id,
                studentName = if (isBroadcast) "All Parents (Broadcast)" else (student?.name ?: "Parent"),
                parentPhone = if (isBroadcast) "ALL" else (student?.parentPhone ?: ""),
                subject = subject,
                messageBody = body,
                category = category.name,
                isBroadcast = isBroadcast
            )
            repository.sendMessageToParent(message)
        }
    }

    fun openWhatsApp(context: Context, phoneNumber: String, messageText: String) {
        try {
            val cleanPhone = phoneNumber.replace("+", "").replace(" ", "").trim()
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(messageText)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openSms(context: Context, phoneNumber: String, messageText: String) {
        try {
            val uri = Uri.parse("smsto:$phoneNumber")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageText)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS app", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Attendance Actions ---
    fun saveAttendanceRecord(student: Student, status: AttendanceStatus, remarks: String) {
        viewModelScope.launch {
            val record = AttendanceRecord(
                studentId = student.id,
                studentName = student.name,
                dateString = _selectedAttendanceDate.value,
                status = status.name,
                remarks = remarks
            )
            repository.saveAttendance(listOf(record))
        }
    }

    fun markAllPresentForToday() {
        viewModelScope.launch {
            val students = allStudents.value
            val todayStr = _selectedAttendanceDate.value
            val records = students.map { s ->
                AttendanceRecord(
                    studentId = s.id,
                    studentName = s.name,
                    dateString = todayStr,
                    status = AttendanceStatus.PRESENT.name
                )
            }
            repository.saveAttendance(records)
        }
    }

    // --- Teacher Tasks / Homework ---
    fun addTask(title: String, description: String, subject: String, batch: String, dueDate: String, category: String) {
        viewModelScope.launch {
            val task = TeacherTask(
                title = title,
                description = description,
                subject = subject,
                batch = batch,
                dueDate = dueDate,
                category = category
            )
            repository.addTask(task)
        }
    }

    fun toggleTask(taskId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(taskId, isCompleted)
        }
    }

    fun deleteTask(task: TeacherTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Test Scores ---
    fun addTestScore(
        student: Student,
        testTitle: String,
        subject: String,
        scoreObtained: Double,
        maxScore: Double,
        remarks: String
    ) {
        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val score = TestScore(
                studentId = student.id,
                studentName = student.name,
                testTitle = testTitle,
                subject = subject,
                scoreObtained = scoreObtained,
                maxScore = maxScore,
                testDate = todayDate,
                remarks = remarks
            )
            repository.addTestScore(score)
        }
    }

    // --- Tuition Expenses ---
    fun addExpense(title: String, category: String, amount: Double, remarks: String) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val exp = TuitionExpense(
                title = title,
                category = category,
                amount = amount,
                dateString = dateStr,
                remarks = remarks
            )
            repository.addExpense(exp)
        }
    }

    fun deleteExpense(expense: TuitionExpense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // --- Student Actions ---
    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    // --- Receipt Sharing ---
    fun shareReceipt(context: Context, receipt: FeeReceipt) {
        try {
            val shareText = """
                ===========================
                 TUITION FEE RECEIPT
                ===========================
                Receipt No: ${receipt.receiptNo}
                Student: ${receipt.studentName}
                Month: ${receipt.monthYear}
                Amount Paid: ₹${receipt.amountPaid}
                Payment Mode: ${receipt.paymentMode}
                Date: ${receipt.paymentDate}
                Remarks: ${receipt.remarks.ifEmpty { "N/A" }}
                ===========================
                Thank you!
            """.trimIndent()

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Receipt Via")
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share receipt: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
