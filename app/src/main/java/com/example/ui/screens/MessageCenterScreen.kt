package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.unit.sp
import com.example.data.MessageCategory
import com.example.data.ParentMessage
import com.example.data.Student
import com.example.ui.theme.FeePaidGreen
import com.example.ui.theme.FeePendingAmber
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
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Compose Message Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compose_message_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Send Message to Parents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Target Audience Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBroadcast) "Broadcast to All Parents" else "Direct Message to Student's Parent",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isBroadcast,
                            onCheckedChange = { isBroadcast = it },
                            modifier = Modifier.testTag("broadcast_switch")
                        )
                    }

                    if (!isBroadcast) {
                        // Student Selector Dropdown
                        ExposedDropdownMenuBox(
                            expanded = studentDropdownExpanded,
                            onExpandedChange = { studentDropdownExpanded = !studentDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedStudent?.let { "${it.name} (${it.parentName})" } ?: "Select Student",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Student & Parent") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = studentDropdownExpanded,
                                onDismissRequest = { studentDropdownExpanded = false }
                            ) {
                                students.forEach { student ->
                                    DropdownMenuItem(
                                        text = { Text("${student.name} • Parent: ${student.parentName}") },
                                        onClick = {
                                            selectedStudent = student
                                            studentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Message Template Quick Chips
                    Text(
                        text = "Quick Template Prompts:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            AssistChip(
                                onClick = {
                                    selectedCategory = MessageCategory.FEE_REMINDER
                                    subject = "Monthly Tuition Fee Reminder"
                                    bodyText = "Dear Parent, this is a polite reminder regarding pending tuition fees for ${selectedStudent?.name ?: "your child"}. Thank you!"
                                },
                                label = { Text("Fee Reminder") },
                                leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        item {
                            AssistChip(
                                onClick = {
                                    selectedCategory = MessageCategory.ANNOUNCEMENT
                                    subject = "Tuition Center Holiday Notice"
                                    bodyText = "Dear Parents, please note that tuition classes will remain closed tomorrow due to upcoming gazetted holiday. Regular schedule resumes day after."
                                },
                                label = { Text("Holiday Notice") },
                                leadingIcon = { Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        item {
                            AssistChip(
                                onClick = {
                                    selectedCategory = MessageCategory.TEST_RESULT
                                    subject = "Upcoming Assessment Test"
                                    bodyText = "Dear Parents, monthly syllabus assessment for ${selectedStudent?.gradeClass ?: "all batches"} is scheduled on Monday. Please supervise revision."
                                },
                                label = { Text("Test Notice") },
                                leadingIcon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                        item {
                            AssistChip(
                                onClick = {
                                    selectedCategory = MessageCategory.ATTENDANCE
                                    subject = "Student Attendance Alert"
                                    bodyText = "Dear Parent, ${selectedStudent?.name ?: "Student"} was absent from today's tuition class. Please notify if leave was planned."
                                },
                                label = { Text("Attendance Alert") },
                                leadingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject / Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = bodyText,
                        onValueChange = { bodyText = it },
                        label = { Text("Message Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (subject.isNotBlank() && bodyText.isNotBlank()) {
                                    onSendMessage(selectedStudent, subject, bodyText, selectedCategory, isBroadcast)
                                    subject = ""
                                    bodyText = ""
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_app_message_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Outbox")
                        }

                        if (!isBroadcast && selectedStudent != null) {
                            OutlinedButton(
                                onClick = {
                                    val fullMsg = "*$subject*\n\n$bodyText\n\n- Tuition Center Teacher"
                                    onSendWhatsApp(context, selectedStudent!!.parentPhone, fullMsg)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF128C7E), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", color = Color(0xFF128C7E))
                            }

                            OutlinedButton(
                                onClick = {
                                    val fullMsg = "$subject: $bodyText"
                                    onSendSms(context, selectedStudent!!.parentPhone, fullMsg)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SMS")
                            }
                        }
                    }
                }
            }
        }

        // Outbox & Sent Messages Log
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Sent Messages History (${messages.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (messages.isEmpty()) {
                    Text(
                        text = "No sent message records available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    messages.forEach { msg ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (msg.isBroadcast) Icons.Default.Campaign else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = msg.studentName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    AssistChip(
                                        onClick = {},
                                        label = { Text(msg.category, fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                }

                                Text(
                                    text = msg.subject,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = msg.messageBody,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(msg.sentTimestamp))
                                Text(
                                    text = "Sent: $dateFormatted ${if (msg.parentPhone.isNotBlank() && msg.parentPhone != "ALL") "• " + msg.parentPhone else ""}",
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
