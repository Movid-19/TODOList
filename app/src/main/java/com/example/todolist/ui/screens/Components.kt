package com.example.todolist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.R
import com.example.todolist.data.Todo
import com.example.todolist.viewmodel.FilterType
import com.example.todolist.viewmodel.SortType
import com.example.todolist.viewmodel.TodoViewModel
import java.util.Date

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
fun TodoSearchFilterBar(viewModel: TodoViewModel) {
    val filterType by viewModel.filterType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("🔍 Search...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            Button(
                onClick = { showSortMenu = true },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("Sort")
            }
        }

        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false }
        ) {
            SortType.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (sort) {
                                SortType.NEWEST_FIRST -> "📅 Newest First"
                                SortType.OLDEST_FIRST -> "📅 Oldest First"
                                SortType.DUE_DATE -> "📆 Due Date"
                                SortType.ALPHABETICAL -> "🔤 Alphabetical"
                            },
                            color = if (sortType == sort) MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    },
                    onClick = {
                        viewModel.setSortType(sort)
                        showSortMenu = false
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterType.entries.forEach { type ->
                FilterChip(
                    selected = filterType == type,
                    onClick = { viewModel.setFilterType(type) },
                    label = {
                        Text(when (type) {
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
fun EmptyStateMessage(message: String) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        text = message,
        fontSize = 18.sp
    )
}

@Composable
fun TodoListItem(
    item: Todo,
    onDelete: () -> Unit,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit,
    onEditDueDate: () -> Unit,
    onEditReminder: () -> Unit
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
            if (!item.isDone) {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("✅ Mark as completed") },
                        onClick = {
                            onToggleDone()
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("📅 Edit due date") },
                        onClick = {
                            onEditDueDate()
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("⏰ Edit reminder") },
                        onClick = {
                            onEditReminder()
                            expanded = false
                        }
                    )
                }
            } else {
                Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.Green)
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
                item.reminderTime?.let {
                    Text(
                        text = "⏰ Reminder: ${formatTime(it)}",
                        fontSize = 12.sp,
                        color = Color.Blue
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.Blue
                )
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
    return java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(date)
}

private fun formatTime(timestamp: Long): String {
    return java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
}

enum class NavTab {
    TASKS, COMPLETED
}