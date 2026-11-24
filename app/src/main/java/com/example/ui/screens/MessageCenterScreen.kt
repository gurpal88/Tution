package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.MessageCategory
import com.example.data.ParentMessage
import com.example.data.Student
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StudentAvatarBadge
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppTint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageCenterScreen(
    students: List<Student>,
    messages: List<ParentMessage>,
    onSendMessage: (student: Student?, subject: String, body: String, category: MessageCategory, isBroadcast: Boolean) -> Unit,
    onSendWhatsApp: (context: Context, phone: String, message: String) -> Unit,
    onSendSms: (context: Context, phone: String, message: String) -> Unit
) {
    val context = LocalContext.current
    var isBroadcast by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(students.firstOrNull()) }
    var subject by remember { mutableStateOf("Fee Reminder Notice") }
    var bodyText by remember {
        mutableStateOf("Dear Parent, this is a friendly reminder that monthly tuition fees for August 2026 are pending. Kindly make the payment at your earliest convenience.")
    }
    var selectedCategory by remember { mutableStateOf(MessageCategory.FEE_REMINDER) }
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Compose Message Studio Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compose_message_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MarkChatUnread,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Parent Communication",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Send direct reminders or batch announcements",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Audience Mode Toggle Container
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isBroadcast) Icons.Default.Campaign else Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (isBroadcast) "Broadcast Mode" else "Direct to Parent",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBroadcast) "Sends to all ${students.size} enrolled parents" else "Single student parent message",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isBroadcast,
                            onCheckedChange = { isBroadcast = it },
                            modifier = Modifier.testTag("broadcast_switch")
                        )
                    }

                    AnimatedVisibility(visible = !isBroadcast) {
                        ExposedDropdownMenuBox(
                            expanded = studentDropdownExpanded,
                            onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedStudent?.let { "${it.name} (${it.parentName})" } ?: "Select Student",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Recipient Student & Parent") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = studentDropdownExpanded,
                                onDismissRequest = { studentDropdownExpanded = false }
                            ) {
                                students.forEach { student ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                StudentAvatarBadge(name = student.name, size = 30.dp)
                                                Column {
                                                    Text(student.name, fontWeight = FontWeight.SemiBold)
                                                    Text(
                                                        "Parent: ${student.parentName}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedStudent = student
                                            studentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Quick Templates
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Quick Templates",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == MessageCategory.FEE_REMINDER,
                                    onClick = {
                                        selectedCategory = MessageCategory.FEE_REMINDER
                                        subject = "Monthly Tuition Fee Reminder"
                                        bodyText = "Dear Parent, this is a polite reminder regarding pending tuition fees for ${selectedStudent?.name ?: "your child"}. Thank you!"
                                    },
                                    label = { Text("Fee Reminder") },
                                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    shape = RoundedCornerShape(50)
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedCategory == MessageCategory.ANNOUNCEMENT,
                                    onClick = {
                                        selectedCategory = MessageCategory.ANNOUNCEMENT
                                        subject = "Tuition Center Holiday Notice"
                                        bodyText = "Dear Parents, please note that tuition classes will remain closed tomorrow due to upcoming gazetted holiday. Regular schedule resumes day after."
                                    },
                                    label = { Text("Holiday Notice") },
                                    leadingIcon = { Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    shape = RoundedCornerShape(50)
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedCategory == MessageCategory.TEST_RESULT,
                                    onClick = {
                                        selectedCategory = MessageCategory.TEST_RESULT
                                        subject = "Upcoming Assessment Test"
                                        bodyText = "Dear Parents, monthly syllabus assessment for ${selectedStudent?.gradeClass ?: "all batches"} is scheduled on Monday. Please supervise revision."
                                    },
                                    label = { Text("Test Notice") },
                                    leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    shape = RoundedCornerShape(50)
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedCategory == MessageCategory.ATTENDANCE,
                                    onClick = {
                                        selectedCategory = MessageCategory.ATTENDANCE
                                        subject = "Student Attendance Alert"
                                        bodyText = "Dear Parent, ${selectedStudent?.name ?: "Student"} was absent from today's tuition class. Please notify if leave was planned."
                                    },
                                    label = { Text("Attendance Alert") },
                                    leadingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    shape = RoundedCornerShape(50)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject / Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = bodyText,
                        onValueChange = { bodyText = it },
                        label = { Text("Message Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 110.dp),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Action Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (subject.isNotBlank() && bodyText.isNotBlank()) {
                                    onSendMessage(selectedStudent, subject, bodyText, selectedCategory, isBroadcast)
                                    subject = ""
                                    bodyText = ""
                                }
                            },
                            enabled = subject.isNotBlank() && bodyText.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("send_app_message_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBroadcast) "Publish Broadcast to Outbox" else "Save to Parent Portal Outbox")
                        }

                        if (!isBroadcast && selectedStudent != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val fullMsg = "*$subject*\n\n$bodyText\n\n- Tuition Center Teacher"
                                        onSendWhatsApp(context, selectedStudent!!.parentPhone, fullMsg)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.45f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = WhatsAppTint.copy(alpha = 0.08f),
                                        contentColor = WhatsAppGreen
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 46.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("WhatsApp")
                                }

                                FilledTonalButton(
                                    onClick = {
                                        val fullMsg = "$subject: $bodyText"
                                        onSendSms(context, selectedStudent!!.parentPhone, fullMsg)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 46.dp)
                                ) {
                                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send SMS")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sent Messages Log
        item {
            SectionHeader(
                title = "Sent History",
                badgeText = "${messages.size}"
            )
        }

        if (messages.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Outlined.MarkEmailRead,
                    title = "Outbox is Empty",
                    subtitle = "Messages and broadcasts you send to parents will be archived here."
                )
            }
        } else {
            items(messages, key = { it.id }) { msg ->
                val dateFormatted = remember(msg.sentTimestamp) {
                    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(msg.sentTimestamp))
                }
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (msg.isBroadcast) MaterialTheme.colorScheme.tertiaryContainer
                                    else MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (msg.isBroadcast) Icons.Default.Campaign else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (msg.isBroadcast) MaterialTheme.colorScheme.tertiary
                                            else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = msg.studentName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = dateFormatted + if (msg.parentPhone.isNotBlank() && msg.parentPhone != "ALL") " • ${msg.parentPhone}" else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = msg.category.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = msg.subject,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = msg.messageBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
