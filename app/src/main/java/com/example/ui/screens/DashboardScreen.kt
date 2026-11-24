package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.FeeReceipt
import com.example.data.FeeStatus
import com.example.data.Student
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DashboardStats

@Composable
fun DashboardScreen(
    stats: DashboardStats,
    students: List<Student>,
    receipts: List<FeeReceipt>,
    selectedMonth: String = "August 2026",
    availableMonths: List<String> = listOf("August 2026", "July 2026", "June 2026", "May 2026", "All Months"),
    onSelectMonth: (String) -> Unit = {},
    onNavigateToFees: (FeeStatus?) -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onQuickAddStudent: () -> Unit,
    onQuickUpdateFee: (Student) -> Unit
) {
    val palette = LocalTuitionStatusPalette.current
    val pendingStudents = students.filter {
        it.feeStatus == FeeStatus.PENDING.name || it.feeStatus == FeeStatus.OVERDUE.name
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Month Filter Pill Strip
        item {
            MonthSelectorPillBar(
                selectedMonth = selectedMonth,
                availableMonths = availableMonths,
                onSelectMonth = onSelectMonth
            )
        }

        // Hero Financial Overview Banner with Custom Gradient & Geometric Canvas Backdrop
        item {
            val collectionRate = if (stats.totalStudents > 0) {
                ((stats.paidCount.toFloat() / stats.totalStudents.toFloat()) * 100).toInt()
            } else 0

            val heroGradient = Brush.linearGradient(
                colors = listOf(
                    SapphireDeep,
                    SapphirePrimary,
                    Color(0xFF134E5E)
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(heroGradient)
                    .drawBehind {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.06f),
                            radius = size.width * 0.38f,
                            center = Offset(size.width * 0.92f, size.height * 0.12f)
                        )
                        drawCircle(
                            color = Color(0xFF2DD4BF).copy(alpha = 0.10f),
                            radius = size.width * 0.24f,
                            center = Offset(size.width * 0.85f, size.height * 0.85f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.04f),
                            radius = size.width * 0.18f,
                            center = Offset(size.width * 0.08f, size.height * 0.9f)
                        )
                    }
                    .testTag("dashboard_hero_card")
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = selectedMonth.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF99F6E4),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "₹${stats.totalCollectedThisMonth.toInt()}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Total Fees Collected",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }

                        // Collection Health Pill
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "$collectionRate%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6EE7B7)
                                )
                                Text(
                                    text = "Collected",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }
                        }
                    }

                    // Segmented Collection Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SegmentedCollectionBar(
                            paidCount = stats.paidCount,
                            pendingCount = stats.pendingCount,
                            overdueCount = stats.overdueCount
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stats.paidCount} of ${stats.totalStudents} students cleared",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                            Text(
                                text = "Pending: ₹${stats.pendingAmountTotal.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE68A)
                            )
                        }
                    }

                    // Net Earnings & Tasks Footer Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF99F6E4),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Net Profit: ₹${stats.netRevenueThisMonth.toInt()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToTasks() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Checklist,
                                contentDescription = null,
                                tint = Color(0xFFFDE68A),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${stats.pendingTasksCount} Open Tasks",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFFDE68A)
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions 2x2 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(title = "Quick Actions")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionTile(
                        title = "Add Student",
                        subtitle = "Enroll new learner",
                        icon = Icons.Default.PersonAdd,
                        accentColor = MaterialTheme.colorScheme.primary,
                        iconBg = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onQuickAddStudent,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_add_student")
                    )
                    QuickActionTile(
                        title = "Update Fee",
                        subtitle = "Collect & receipt",
                        icon = Icons.Default.Payments,
                        accentColor = palette.paid,
                        iconBg = palette.paidContainer,
                        onClick = { onNavigateToFees(FeeStatus.PENDING) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_update_fee")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionTile(
                        title = "Message Parent",
                        subtitle = "WhatsApp or SMS",
                        icon = Icons.AutoMirrored.Filled.Send,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        iconBg = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = onNavigateToMessages,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_message_parents")
                    )
                    QuickActionTile(
                        title = "Attendance",
                        subtitle = "Daily roll & marks",
                        icon = Icons.Default.HowToReg,
                        accentColor = palette.pending,
                        iconBg = palette.pendingContainer,
                        onClick = onNavigateToAttendance,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_attendance")
                    )
                }
            }
        }

        // Student Fee Breakdown Trio
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = "Fee Status Breakdown",
                    badgeText = "${stats.totalStudents} Total",
                    actionText = "View All",
                    onActionClick = { onNavigateToFees(null) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatusCard(
                        modifier = Modifier.weight(1f),
                        title = "Paid",
                        count = stats.paidCount,
                        total = stats.totalStudents,
                        icon = Icons.Default.CheckCircle,
                        accentColor = palette.paid,
                        containerColor = palette.paidContainer,
                        onClick = { onNavigateToFees(FeeStatus.PAID) }
                    )
                    MetricStatusCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending",
                        count = stats.pendingCount,
                        total = stats.totalStudents,
                        icon = Icons.Default.Schedule,
                        accentColor = palette.pending,
                        containerColor = palette.pendingContainer,
                        onClick = { onNavigateToFees(FeeStatus.PENDING) }
                    )
                    MetricStatusCard(
                        modifier = Modifier.weight(1f),
                        title = "Overdue",
                        count = stats.overdueCount,
                        total = stats.totalStudents,
                        icon = Icons.Default.WarningAmber,
                        accentColor = palette.overdue,
                        containerColor = palette.overdueContainer,
                        onClick = { onNavigateToFees(FeeStatus.OVERDUE) }
                    )
                }
            }
        }

        // Pending Fee Alerts Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = "Requires Attention",
                    badgeText = "${pendingStudents.size}",
                    actionText = if (pendingStudents.size > 3) "See All" else null,
                    onActionClick = if (pendingStudents.size > 3) {
                        { onNavigateToFees(FeeStatus.PENDING) }
                    } else null
                )

                if (pendingStudents.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.paidContainer),
                        border = BorderStroke(1.dp, palette.paid.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = palette.paid,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "All Caught Up!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.paid
                                )
                                Text(
                                    text = "Every student's fee for this cycle is cleared.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.paid.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        pendingStudents.take(3).forEach { student ->
                            val isOverdue = student.feeStatus == FeeStatus.OVERDUE.name
                            val statusEnum = if (isOverdue) FeeStatus.OVERDUE else FeeStatus.PENDING
                            val accent = if (isOverdue) palette.overdue else palette.pending

                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    StudentAvatarBadge(name = student.name, size = 44.dp)

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            StatusPillBadge(status = statusEnum)
                                        }
                                        Text(
                                            text = "${student.gradeClass} • Due ${student.dueDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "₹${student.monthlyFee.toInt()} due",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = accent
                                        )
                                    }

                                    Button(
                                        onClick = { onQuickUpdateFee(student) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = accent,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                        modifier = Modifier.heightIn(min = 44.dp)
                                    ) {
                                        Text("Mark Paid", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Fee Receipts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = "Recent Receipts",
                    badgeText = "${receipts.size}",
                    actionText = "All Receipts",
                    onActionClick = { onNavigateToFees(null) }
                )

                if (receipts.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = "No Receipts Yet",
                        subtitle = "Generated payment receipts for $selectedMonth will appear here."
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        receipts.take(4).forEach { receipt ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
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
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = palette.paidContainer,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                                    contentDescription = null,
                                                    tint = palette.paid,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = receipt.studentName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${receipt.monthYear} • ${receipt.paymentMode}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "+₹${receipt.amountPaid.toInt()}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = palette.paid
                                        )
                                        Text(
                                            text = receipt.receiptNo,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = iconBg,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MetricStatusCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    total: Int,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
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
                Surface(
                    shape = CircleShape,
                    color = containerColor,
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Column {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "of $total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
