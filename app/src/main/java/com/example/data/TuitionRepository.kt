package com.example.data

import kotlinx.coroutines.flow.Flow

class TuitionRepository(private val dao: TuitionDao) {

    val allStudents: Flow<List<Student>> = dao.getAllStudents()
    val allReceipts: Flow<List<FeeReceipt>> = dao.getAllReceipts()
    val allMessages: Flow<List<ParentMessage>> = dao.getAllMessages()
    val allTasks: Flow<List<TeacherTask>> = dao.getAllTasks()
    val allTestScores: Flow<List<TestScore>> = dao.getAllTestScores()
    val allExpenses: Flow<List<TuitionExpense>> = dao.getAllExpenses()

    fun getStudentsByFeeStatus(status: String): Flow<List<Student>> =
        dao.getStudentsByFeeStatus(status)

    suspend fun insertStudent(student: Student): Long =
        dao.insertStudent(student)

    suspend fun updateStudent(student: Student) =
        dao.updateStudent(student)

    suspend fun deleteStudent(student: Student) =
        dao.deleteStudent(student)

    suspend fun markFeeAsPaid(
        studentId: Int,
        studentName: String,
        amount: Double,
        monthYear: String,
        paymentMode: String,
        remarks: String
    ) {
        val receiptNo = "RCP-${System.currentTimeMillis().toString().takeLast(6)}"
        val receipt = FeeReceipt(
            studentId = studentId,
            studentName = studentName,
            monthYear = monthYear,
            amountPaid = amount,
            paymentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            paymentMode = paymentMode,
            receiptNo = receiptNo,
            remarks = remarks
        )
        dao.insertReceipt(receipt)
        dao.updateFeeStatus(studentId, FeeStatus.PAID.name, monthYear)
    }

    suspend fun updateStudentFeeStatusDirectly(studentId: Int, status: String, monthYear: String) {
        dao.updateFeeStatus(studentId, status, monthYear)
    }

    suspend fun sendMessageToParent(message: ParentMessage): Long =
        dao.insertMessage(message)

    fun getAttendanceForDate(dateString: String): Flow<List<AttendanceRecord>> =
        dao.getAttendanceForDate(dateString)

    suspend fun saveAttendance(records: List<AttendanceRecord>) {
        records.forEach { record ->
            dao.deleteAttendanceForStudentOnDate(record.studentId, record.dateString)
        }
        dao.insertAllAttendance(records)
    }

    suspend fun addTask(task: TeacherTask): Long =
        dao.insertTask(task)

    suspend fun toggleTaskCompletion(taskId: Int, isCompleted: Boolean) =
        dao.updateTaskCompletion(taskId, isCompleted)

    suspend fun deleteTask(task: TeacherTask) =
        dao.deleteTask(task)

    suspend fun addTestScore(score: TestScore): Long =
        dao.insertTestScore(score)

    suspend fun addExpense(expense: TuitionExpense): Long =
        dao.insertExpense(expense)

    suspend fun deleteExpense(expense: TuitionExpense) =
        dao.deleteExpense(expense)
}
