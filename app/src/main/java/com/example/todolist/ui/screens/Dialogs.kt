package com.example.todolist.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Date

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (title: String, dueDate: Date?, reminderTime: Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCalendar = Calendar.getInstance().apply {
                                    set(year, month, dayOfMonth, 0, 0, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                selectedDate = selectedCalendar.time
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(if (selectedDate == null) "📅 Set Due Date" else "📅 Due: ${formatDate(selectedDate!!)}")
                }

                if (selectedDate != null) {
                    Button(
                        onClick = { selectedDate = null },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Clear Date")
                    }
                }

                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        reminderTime?.let { calendar.timeInMillis = it }
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                val selectedCalendar = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                if (selectedCalendar.timeInMillis > System.currentTimeMillis()) {
                                    reminderTime = selectedCalendar.timeInMillis
                                } else {
                                    Toast.makeText(context, "Please select a future time", Toast.LENGTH_SHORT).show()
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        if (reminderTime == null) "⏰ Set Reminder Time"
                        else "⏰ Reminder: ${formatTime(reminderTime!!)}"
                    )
                }

                if (reminderTime != null) {
                    Button(
                        onClick = { reminderTime = null },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Clear Reminder")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(title, selectedDate, reminderTime)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditTaskDialog(
    currentTitle: String,
    onTitleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            OutlinedTextField(
                value = currentTitle,
                onValueChange = onTitleChange,
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditDueDateDialog(
    currentDate: Date?,
    onDismiss: () -> Unit,
    onSave: (Date?) -> Unit
) {
    var selectedDate by remember { mutableStateOf(currentDate) }
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Due Date") },
        text = {
            Column {
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (selectedDate == null) "📅 Set Due Date"
                        else "📅 Due: ${formatDate(selectedDate!!)}"
                    )
                }

                if (selectedDate != null) {
                    Button(
                        onClick = { selectedDate = null },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text("Clear Date")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selectedDate) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        val calendar = Calendar.getInstance().apply {
            selectedDate?.let { time = it }
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDate = selectedCalendar.time
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { showDatePicker = false }
        }.show()
    }
}

@Composable
fun EditReminderDialog(
    currentReminder: Long?,
    taskId: String,
    taskTitle: String,
    onDismiss: () -> Unit,
    onUpdateReminder: (String, Long?) -> Unit
) {
    var selectedTime by remember { mutableStateOf(currentReminder) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Reminder") },
        text = {
            Column {
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        selectedTime?.let { calendar.timeInMillis = it }
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                val selectedCalendar = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                if (selectedCalendar.timeInMillis > System.currentTimeMillis()) {
                                    selectedTime = selectedCalendar.timeInMillis
                                } else {
                                    Toast.makeText(context, "Please select a future time", Toast.LENGTH_SHORT).show()
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (selectedTime == null) "⏰ Set Reminder Time"
                        else "⏰ Reminder: ${formatTime(selectedTime!!)}"
                    )
                }

                if (selectedTime != null) {
                    Button(
                        onClick = { selectedTime = null },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text("Clear Reminder")
                    }
                }

                Text(
                    text = "Current reminder: ${if (currentReminder == null) "None" else formatTime(currentReminder)}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onUpdateReminder(taskId, selectedTime) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SignOutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign Out") },
        text = { Text("Are you sure you want to sign out?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Sign Out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(date: Date): String {
    return java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(date)
}

private fun formatTime(timestamp: Long): String {
    return java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
}