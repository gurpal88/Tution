package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FeeStatus {
    PAID, PENDING, OVERDUE
}

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE
}

enum class MessageCategory {
    FEE_REMINDER, ATTENDANCE, HOMEWORK, ANNOUNCEMENT, TEST_RESULT, GENERAL
}

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val gradeClass: String,
    val parentName: String,
    val parentPhone: String,
    val monthlyFee: Double,
    val feeStatus: String = FeeStatus.PENDING.name,
    val dueDate: String = "5th of every month",
    val batchTiming: String = "4:00 PM - 5:30 PM",
    val joiningDate: String = "Jan 2026",
    val lastPaidMonth: String = "July 2026",
    val notes: String = "",
    val email: String = ""
)

@Entity(tableName = "fee_receipts")
data class FeeReceipt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val studentName: String,
    val monthYear: String,
    val amountPaid: Double,
    val paymentDate: String,
    val paymentMode: String = "UPI",
    val receiptNo: String,
    val remarks: String = ""
)

@Entity(tableName = "parent_messages")
data class ParentMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int? = null,
    val studentName: String = "All Parents (Broadcast)",
    val parentPhone: String = "",
    val subject: String,
    val messageBody: String,
    val sentTimestamp: Long = System.currentTimeMillis(),
    val category: String = MessageCategory.GENERAL.name,
    val isBroadcast: Boolean = false
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val studentName: String,
    val dateString: String, // YYYY-MM-DD
    val status: String = AttendanceStatus.PRESENT.name,
    val remarks: String = ""
)

@Entity(tableName = "teacher_tasks")
data class TeacherTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val subject: String,
    val batch: String,
    val dueDate: String,
    val isCompleted: Boolean = false,
    val category: String = "HOMEWORK"
)

@Entity(tableName = "test_scores")
data class TestScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val studentName: String,
    val testTitle: String,
    val subject: String,
    val scoreObtained: Double,
    val maxScore: Double = 100.0,
    val testDate: String,
    val remarks: String = ""
)

@Entity(tableName = "tuition_expenses")
data class TuitionExpense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String = "Rent", // Rent, Electricity, Stationary, Printing, Salary, Other
    val amount: Double,
    val dateString: String,
    val remarks: String = ""
)
