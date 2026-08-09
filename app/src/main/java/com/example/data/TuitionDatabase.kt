package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Student::class,
        FeeReceipt::class,
        ParentMessage::class,
        AttendanceRecord::class,
        TeacherTask::class,
        TestScore::class,
        TuitionExpense::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TuitionDatabase : RoomDatabase() {

    abstract fun tuitionDao(): TuitionDao

    companion object {
        @Volatile
        private var INSTANCE: TuitionDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TuitionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TuitionDatabase::class.java,
                    "tuition_manager_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(TuitionDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class TuitionDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.tuitionDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(dao: TuitionDao) {
            // Seed Students
            val s1 = Student(
                id = 1,
                name = "Aarav Sharma",
                gradeClass = "Class 10 - Mathematics",
                parentName = "Rajesh Sharma",
                parentPhone = "+919876543210",
                monthlyFee = 1500.0,
                feeStatus = FeeStatus.PAID.name,
                dueDate = "5th of every month",
                batchTiming = "4:00 PM - 5:30 PM",
                lastPaidMonth = "August 2026"
            )
            val s2 = Student(
                id = 2,
                name = "Ananya Verma",
                gradeClass = "Class 10 - Science & Maths",
                parentName = "Suresh Verma",
                parentPhone = "+919812345678",
                monthlyFee = 2000.0,
                feeStatus = FeeStatus.PENDING.name,
                dueDate = "5th of every month",
                batchTiming = "4:00 PM - 5:30 PM",
                lastPaidMonth = "July 2026"
            )
            val s3 = Student(
                id = 3,
                name = "Rohan Gupta",
                gradeClass = "Class 12 - Physics",
                parentName = "Sunil Gupta",
                parentPhone = "+919988776655",
                monthlyFee = 2500.0,
                feeStatus = FeeStatus.OVERDUE.name,
                dueDate = "1st of every month",
                batchTiming = "6:00 PM - 7:30 PM",
                lastPaidMonth = "June 2026"
            )
            val s4 = Student(
                id = 4,
                name = "Priya Patel",
                gradeClass = "Class 9 - English & Social",
                parentName = "Mahesh Patel",
                parentPhone = "+919765432109",
                monthlyFee = 1200.0,
                feeStatus = FeeStatus.PAID.name,
                dueDate = "10th of every month",
                batchTiming = "3:00 PM - 4:00 PM",
                lastPaidMonth = "August 2026"
            )
            val s5 = Student(
                id = 5,
                name = "Kabir Singh",
                gradeClass = "Class 11 - Chemistry",
                parentName = "Gurpreet Singh",
                parentPhone = "+919898989898",
                monthlyFee = 1800.0,
                feeStatus = FeeStatus.PENDING.name,
                dueDate = "5th of every month",
                batchTiming = "6:00 PM - 7:30 PM",
                lastPaidMonth = "July 2026"
            )

            dao.insertStudent(s1)
            dao.insertStudent(s2)
            dao.insertStudent(s3)
            dao.insertStudent(s4)
            dao.insertStudent(s5)

            // Seed Receipts
            dao.insertReceipt(
                FeeReceipt(
                    studentId = 1,
                    studentName = "Aarav Sharma",
                    monthYear = "August 2026",
                    amountPaid = 1500.0,
                    paymentDate = "2026-08-03",
                    paymentMode = "UPI",
                    receiptNo = "RCP-2026-001",
                    remarks = "Received via GPay"
                )
            )
            dao.insertReceipt(
                FeeReceipt(
                    studentId = 4,
                    studentName = "Priya Patel",
                    monthYear = "August 2026",
                    amountPaid = 1200.0,
                    paymentDate = "2026-08-05",
                    paymentMode = "Cash",
                    receiptNo = "RCP-2026-002",
                    remarks = "Paid in cash"
                )
            )

            // Seed Parent Messages
            dao.insertMessage(
                ParentMessage(
                    studentId = 2,
                    studentName = "Ananya Verma",
                    parentPhone = "+919812345678",
                    subject = "Monthly Tuition Fee Due",
                    messageBody = "Dear Parent, this is a friendly reminder that Ananya's tuition fee of ₹2000 for August 2026 is pending. Kindly clear at your earliest convenience.",
                    category = MessageCategory.FEE_REMINDER.name,
                    sentTimestamp = System.currentTimeMillis() - 86400000L * 2
                )
            )
            dao.insertMessage(
                ParentMessage(
                    studentId = null,
                    studentName = "All Parents (Broadcast)",
                    parentPhone = "ALL",
                    subject = "Upcoming Monthly Test Schedule",
                    messageBody = "Dear Parents, monthly assessment tests for Class 9 to 12 will begin from Monday, 17th August. Please ensure regular study.",
                    category = MessageCategory.ANNOUNCEMENT.name,
                    isBroadcast = true,
                    sentTimestamp = System.currentTimeMillis() - 86400000L
                )
            )

            // Seed Teacher Tasks / Homework
            dao.insertTask(
                TeacherTask(
                    title = "Prepare Physics Chapter 4 Quiz",
                    description = "Draft 15 multiple choice questions for Class 12 batch.",
                    subject = "Physics",
                    batch = "Class 12",
                    dueDate = "12th Aug 2026",
                    isCompleted = false,
                    category = "EXAM_PREP"
                )
            )
            dao.insertTask(
                TeacherTask(
                    title = "Check Algebra Practice Sheets",
                    description = "Evaluate Class 10 homework submitted yesterday.",
                    subject = "Mathematics",
                    batch = "Class 10",
                    dueDate = "10th Aug 2026",
                    isCompleted = true,
                    category = "HOMEWORK"
                )
            )

            // Seed Test Scores
            dao.insertTestScore(
                TestScore(
                    studentId = 1,
                    studentName = "Aarav Sharma",
                    testTitle = "Quadratic Equations Unit Test",
                    subject = "Mathematics",
                    scoreObtained = 46.5,
                    maxScore = 50.0,
                    testDate = "2026-08-01",
                    remarks = "Excellent problem solving!"
                )
            )
            dao.insertTestScore(
                TestScore(
                    studentId = 2,
                    studentName = "Ananya Verma",
                    testTitle = "Light & Optics Test",
                    subject = "Science",
                    scoreObtained = 42.0,
                    maxScore = 50.0,
                    testDate = "2026-08-02",
                    remarks = "Good work, review ray diagrams."
                )
            )

            // Seed Tuition Expenses
            dao.insertExpense(
                TuitionExpense(
                    title = "Classroom Rent - August",
                    category = "Rent",
                    amount = 3500.0,
                    dateString = "2026-08-01",
                    remarks = "Monthly hall rent paid to landlord"
                )
            )
            dao.insertExpense(
                TuitionExpense(
                    title = "Worksheets & Test Paper Printing",
                    category = "Printing",
                    amount = 650.0,
                    dateString = "2026-08-04",
                    remarks = "Class 10 & 12 test sheets printout"
                )
            )
        }
    }
}
