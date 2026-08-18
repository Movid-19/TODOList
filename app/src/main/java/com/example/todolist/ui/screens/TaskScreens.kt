package com.example.todolist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.todolist.data.Todo
import com.example.todolist.viewmodel.TodoViewModel

@Composable
fun PendingTasksScreen(
    viewModel: TodoViewModel,
    onEditTask: (Todo) -> Unit,
    onEditDueDate: (Todo) -> Unit,
    onEditReminder: (Todo) -> Unit,
    onDeleteWithUndo: (Todo) -> Unit
) {
    val pendingTodos by viewModel.pendingTodos.collectAsState()

    if (pendingTodos.isEmpty()) {
        EmptyStateMessage("No pending tasks! 🎉")
    } else {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            itemsIndexed(
                items = pendingTodos,
                key = { _, item -> item.id }
            ) { _, item ->
                TodoListItem(
                    item = item,
                    onDelete = { onDeleteWithUndo(item) },
                    onToggleDone = { viewModel.toggleDone(item.id, item.isDone) },
                    onEdit = { onEditTask(item) },
                    onEditDueDate = { onEditDueDate(item) },
                    onEditReminder = { onEditReminder(item) }
                )
            }
        }
    }
}

@Composable
fun CompletedTasksScreen(
    viewModel: TodoViewModel,
    onEditTask: (Todo) -> Unit,
    onEditDueDate: (Todo) -> Unit,
    onEditReminder: (Todo) -> Unit,
    onDeleteWithUndo: (Todo) -> Unit
) {
    val completedTodos by viewModel.completedTodos.collectAsState()
    val context = LocalContext.current

    if (completedTodos.isEmpty()) {
        EmptyStateMessage("No completed tasks match the filter")
    } else {
        LazyColumn {
            itemsIndexed(
                items = completedTodos,
                key = { _, item -> item.id }
            ) { _, item ->
                TodoListItem(
                    item = item,
                    onDelete = { onDeleteWithUndo(item) },
                    onToggleDone = {
                        viewModel.toggleDone(item.id, item.isDone)
                        Toast.makeText(context, "Task restored!", Toast.LENGTH_SHORT).show()
                    },
                    onEdit = { onEditTask(item) },
                    onEditDueDate = { onEditDueDate(item) },
                    onEditReminder = { onEditReminder(item) }
                )
            }
        }
    }
}