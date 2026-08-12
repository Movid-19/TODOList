package com.example.todolist

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class NavTab {
    TASKS, COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TodoViewModel,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(NavTab.TASKS) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TodoTopBar(onSignOutClick = { showSignOutDialog = true })
        },
        bottomBar = {
            TodoBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        floatingActionButton = {
            Button(onClick = { showAddDialog = true }) {
                Text("+ Add Task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            TodoSearchFilterBar(viewModel)
            
            when (selectedTab) {
                NavTab.TASKS -> PendingTasksScreen(viewModel)
                NavTab.COMPLETED -> CompletedTasksScreen(viewModel)
            }
        }

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onAddTask = { title, dueDate ->
                    viewModel.addTodo(title, dueDate)
                    showAddDialog = false
                    Toast.makeText(context, "Task added!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showSignOutDialog) {
            SignOutConfirmationDialog(
                onDismiss = { showSignOutDialog = false },
                onConfirm = {
                    showSignOutDialog = false
                    onSignOut()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTopBar(onSignOutClick: () -> Unit) {
    TopAppBar(
        title = { Text("TODO List") },
        actions = {
            IconButton(onClick = onSignOutClick) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out")
            }
        }
    )
}

@Composable
fun TodoSearchFilterBar(viewModel: TodoViewModel) {
    val filterType by viewModel.filterType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("🔍 Search tasks...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterType.entries.forEach { type ->
                FilterChip(
                    selected = filterType == type,
                    onClick = { viewModel.setFilterType(type) },
                    label = { 
                        Text(when(type) {
                            FilterType.ALL -> "All"
                            FilterType.TODAY -> "Today"
                            FilterType.THIS_WEEK -> "This Week"
                            FilterType.THIS_MONTH -> "This Month"
                            FilterType.OVERDUE -> "⏰ Overdue"
                        })
                    }
                )
            }
        }
    }
}

@Composable
fun TodoBottomBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val tabs = listOf(
                NavTab.TASKS to "📋 Tasks",
                NavTab.COMPLETED to "✅ Completed"
            )
            tabs.forEach { (tab, label) ->
                Button(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                    enabled = selectedTab != tab
                ) {
                    Text(label)
                }
            }
        }
    }
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

@Composable
fun PendingTasksScreen(viewModel: TodoViewModel) {
    val pendingTodos by viewModel.pendingTodos.collectAsState()

    if (pendingTodos.isEmpty()) {
        EmptyStateMessage("No pending tasks! 🎉")
    } else {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            itemsIndexed(pendingTodos) { _, item ->
                TodoListItem(
                    item = item,
                    onDelete = { viewModel.deleteTodo(item.id) },
                    onToggleDone = { viewModel.toggleDone(item.id, item.isDone) }
                )
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        textAlign = TextAlign.Center,
        text = message,
        fontSize = 18.sp
    )
}

@Composable
fun CompletedTasksScreen(viewModel: TodoViewModel) {
    val completedTodos by viewModel.completedTodos.collectAsState()
    val context = LocalContext.current

    if (completedTodos.isEmpty()) {
        EmptyStateMessage("No completed tasks match the filter")
    } else {
        LazyColumn {
            itemsIndexed(completedTodos) { _, item ->
                TodoListItem(
                    item = item,
                    onDelete = { viewModel.deleteTodo(item.id) },
                    onToggleDone = {
                        viewModel.toggleDone(item.id, item.isDone)
                        Toast.makeText(context, "Task restored!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (title: String, dueDate: Date?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
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
                    Text(
                        if (selectedDate == null) "📅 Set Due Date"
                        else "📅 Due: ${formatDate(selectedDate!!)}"
                    )
                }

                if (selectedDate != null) {
                    Button(
                        onClick = { selectedDate = null },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Clear Date")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(title, selectedDate)
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
fun TodoListItem(
    item: Todo,
    onDelete: () -> Unit,
    onToggleDone: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.isDone) {
                Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.Green)
            } else {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Mark as completed") },
                        onClick = {
                            onToggleDone()
                            expanded = false
                        }
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 18.sp,
                    textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                    color = if (item.isDone) Color.Gray else Color.Unspecified
                )
                Text(
                    text = if (item.isDone) "Completed: ${formatDate(item.createdAt)}" else "Created: ${formatDate(item.createdAt)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                item.dueDate?.let {
                    val isOverdue = !item.isDone && it.before(Date())
                    Text(
                        text = "Due: ${formatDate(it)}",
                        fontSize = 12.sp,
                        color = if (isOverdue) Color.Red else Color.Gray
                    )
                }
            }

            if (item.isDone) {
                Button(onClick = onToggleDone) {
                    Text("↩️ Restore")
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_delete_24),
                    contentDescription = "Delete"
                )
            }
        }
    }
}

private fun formatDate(date: Date): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
}
