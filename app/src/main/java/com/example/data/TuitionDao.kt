package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TuitionDao {

    // --- Students & Fee Management ---
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Int): Student?

    @Query("SELECT * FROM students WHERE feeStatus = :status ORDER BY name ASC")
    fun getStudentsByFeeStatus(status: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("UPDATE students SET feeStatus = :status, lastPaidMonth = :monthYear WHERE id = :studentId")
    suspend fun updateFeeStatus(studentId: Int, status: String, monthYear: String)

    // --- Fee Receipts ---
    @Query("SELECT * FROM fee_receipts ORDER BY id DESC")
    fun getAllReceipts(): Flow<List<FeeReceipt>>

    @Query("SELECT * FROM fee_receipts WHERE studentId = :studentId ORDER BY id DESC")
    fun getReceiptsForStudent(studentId: Int): Flow<List<FeeReceipt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: FeeReceipt): Long

    // --- Parent Messages ---
    @Query("SELECT * FROM parent_messages ORDER BY sentTimestamp DESC")
    fun getAllMessages(): Flow<List<ParentMessage>>

    @Query("SELECT * FROM parent_messages WHERE studentId = :studentId OR isBroadcast = 1 ORDER BY sentTimestamp DESC")
    fun getMessagesForStudent(studentId: Int): Flow<List<ParentMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ParentMessage): Long

    // --- Attendance Records ---
    @Query("SELECT * FROM attendance_records WHERE dateString = :dateString")
    fun getAttendanceForDate(dateString: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY dateString DESC")
    fun getAttendanceForStudent(studentId: Int): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE studentId = :studentId AND dateString = :dateString")
    suspend fun deleteAttendanceForStudentOnDate(studentId: Int, dateString: String)

    // --- Teacher Tasks / Homework ---
    @Query("SELECT * FROM teacher_tasks ORDER BY isCompleted ASC, id DESC")
    fun getAllTasks(): Flow<List<TeacherTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TeacherTask): Long

    @Query("UPDATE teacher_tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean)

    @Delete
    suspend fun deleteTask(task: TeacherTask)

    // --- Test Scores ---
    @Query("SELECT * FROM test_scores ORDER BY testDate DESC")
    fun getAllTestScores(): Flow<List<TestScore>>

    @Query("SELECT * FROM test_scores WHERE studentId = :studentId ORDER BY testDate DESC")
    fun getTestScoresForStudent(studentId: Int): Flow<List<TestScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestScore(testScore: TestScore): Long

    // --- Tuition Expenses ---
    @Query("SELECT * FROM tuition_expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<TuitionExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: TuitionExpense): Long

    @Delete
    suspend fun deleteExpense(expense: TuitionExpense)
}
