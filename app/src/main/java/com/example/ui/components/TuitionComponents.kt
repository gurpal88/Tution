package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FeeReceipt
import com.example.data.FeeStatus
import com.example.ui.theme.*
import kotlin.math.abs

@Composable
fun MonthSelectorPillBar(
    selectedMonth: String,
    availableMonths: List<String>,
    onSelectMonth: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Selected Month",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(availableMonths) { month ->
                val isSelected = selectedMonth == month
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface,
                    animationSpec = tween(180),
                    label = "monthBg"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(180),
                    label = "monthFg"
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = bgColor,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .heightIn(min = 36.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { onSelectMonth(month) }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = month,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAvatarBadge(
    name: String,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val initials = name
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifEmpty { "S" }

    val colorIdx = abs(name.hashCode()) % AvatarBgColors.size
    val statusPalette = LocalTuitionStatusPalette.current
    val bgColor = if (statusPalette.isDark) {
        AvatarFgColors[colorIdx].copy(alpha = 0.25f)
    } else {
        AvatarBgColors[colorIdx]
    }
    val fgColor = if (statusPalette.isDark) {
        AvatarBgColors[colorIdx]
    } else {
        AvatarFgColors[colorIdx]
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, fgColor.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = fgColor,
            fontSize = (size.value * 0.34f).sp
        )
    }
}

@Composable
fun StatusPillBadge(
    status: FeeStatus,
    modifier: Modifier = Modifier
) {
    val palette = LocalTuitionStatusPalette.current
    val (fg, bg, label) = when (status) {
        FeeStatus.PAID -> Triple(palette.paid, palette.paidContainer, "PAID")
        FeeStatus.PENDING -> Triple(palette.pending, palette.pendingContainer, "PENDING")
        FeeStatus.OVERDUE -> Triple(palette.overdue, palette.overdueContainer, "OVERDUE")
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.22f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = fg
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    badgeText: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SegmentedCollectionBar(
    paidCount: Int,
    pendingCount: Int,
    overdueCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (paidCount + pendingCount + overdueCount).coerceAtLeast(1)
    val paidRatio by animateFloatAsState(
        targetValue = paidCount.toFloat() / total.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "paidRatio"
    )
    val pendingRatio by animateFloatAsState(
        targetValue = pendingCount.toFloat() / total.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pendingRatio"
    )
    val overdueRatio by animateFloatAsState(
        targetValue = overdueCount.toFloat() / total.toFloat(),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "overdueRatio"
    )

    val palette = LocalTuitionStatusPalette.current
    val trackColor = Color.White.copy(alpha = 0.16f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
    ) {
        val w = size.width
        val h = size.height
        val gap = 3.dp.toPx()

        var xOffset = 0f
        if (paidRatio > 0f) {
            val segW = (w * paidRatio - if (pendingRatio > 0f || overdueRatio > 0f) gap / 2 else 0f).coerceAtLeast(0f)
            drawRoundRect(
                color = Color(0xFF34D399),
                topLeft = Offset(xOffset, 0f),
                size = androidx.compose.ui.geometry.Size(segW, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2)
            )
            xOffset += segW + gap
        }
        if (pendingRatio > 0f) {
            val segW = (w * pendingRatio - if (overdueRatio > 0f) gap / 2 else 0f).coerceAtLeast(0f)
            drawRoundRect(
                color = Color(0xFFFBBF24),
                topLeft = Offset(xOffset, 0f),
                size = androidx.compose.ui.geometry.Size(segW, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2)
            )
            xOffset += segW + gap
        }
        if (overdueRatio > 0f) {
            val segW = (w - xOffset).coerceAtLeast(0f)
            drawRoundRect(
                color = palette.overdue.copy(alpha = 0.95f),
                topLeft = Offset(xOffset, 0f),
                size = androidx.compose.ui.geometry.Size(segW, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2, h / 2)
            )
        }
    }
}

@Composable
fun TicketReceiptCard(
    receipt: FeeReceipt,
    onShare: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalTuitionStatusPalette.current
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, outlineColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = palette.paidContainer,
                        modifier = Modifier.size(42.dp)
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = palette.paid
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = receipt.receiptNo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Perforated dashed ticket divider
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(1.dp)
            ) {
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Paid on ${receipt.paymentDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (receipt.remarks.isNotBlank()) {
                        Text(
                            text = receipt.remarks,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (onShare != null) {
                    FilledTonalButton(
                        onClick = onShare,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.heightIn(min = 38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Receipt",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun CircularScoreBadge(
    percentage: Float,
    color: Color,
    gradeText: String,
    modifier: Modifier = Modifier
) {
    val animatedPct by animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(550),
        label = "scoreSweep"
    )
    val trackColor = color.copy(alpha = 0.16f)

    Box(
        modifier = modifier.size(54.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 5.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedPct,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = gradeText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
