package com.example.todolist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.todolist.data.Todo
import com.example.todolist.viewmodel.TodoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TodoViewModel,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(NavTab.TASKS) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    var editingItem by remember { mutableStateOf<Todo?>(null) }
    var editText by remember { mutableStateOf("") }

    var editingDueDateItem by remember { mutableStateOf<Todo?>(null) }

    var editingReminderItem by remember { mutableStateOf<Todo?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var deletedTaskForUndo by remember { mutableStateOf<Todo?>(null) }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            TodoSearchFilterBar(viewModel)

            when (selectedTab) {
                NavTab.TASKS -> PendingTasksScreen(
                    viewModel = viewModel,
                    onEditTask = { todo ->
                        editingItem = todo
                        editText = todo.title
                    },
                    onEditDueDate = { todo ->
                        editingDueDateItem = todo
                    },
                    onEditReminder = { todo ->
                        editingReminderItem = todo
                    },
                    onDeleteWithUndo = { todo ->
                        deletedTaskForUndo = todo
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Task deleted",
                                actionLabel = "Undo",
                                duration = androidx.compose.material3.SnackbarDuration.Short
                            )
                            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                viewModel.undoDelete()
                            }
                            deletedTaskForUndo = null
                        }
                        viewModel.deleteTodoWithUndo(todo.id) { deleted ->
                        }
                    }
                )
                NavTab.COMPLETED -> CompletedTasksScreen(
                    viewModel = viewModel,
                    onEditTask = { todo ->
                        editingItem = todo
                        editText = todo.title
                    },
                    onEditDueDate = { todo ->
                        editingDueDateItem = todo
                    },
                    onEditReminder = { todo ->
                        editingReminderItem = todo
                    },
                    onDeleteWithUndo = { todo ->
                        deletedTaskForUndo = todo
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Task deleted",
                                actionLabel = "Undo",
                                duration = androidx.compose.material3.SnackbarDuration.Short
                            )
                            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                viewModel.undoDelete()
                            }
                            deletedTaskForUndo = null
                        }
                        viewModel.deleteTodoWithUndo(todo.id) { deleted ->
                        }
                    }
                )
            }
        }

        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onAddTask = { title, dueDate, reminderTime ->
                    viewModel.addTodo(title, dueDate, reminderTime)
                    showAddDialog = false
                    Toast.makeText(context, "Task added!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (editingItem != null) {
            EditTaskDialog(
                currentTitle = editText,
                onTitleChange = { editText = it },
                onDismiss = { editingItem = null },
                onSave = {
                    editingItem?.let { todo ->
                        if (editText.isNotBlank()) {
                            viewModel.editTodo(todo.id, editText)
                            Toast.makeText(context, "Task updated!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    editingItem = null
                }
            )
        }

        if (editingDueDateItem != null) {
            EditDueDateDialog(
                currentDate = editingDueDateItem?.dueDate,
                onDismiss = { editingDueDateItem = null },
                onSave = { newDate ->
                    editingDueDateItem?.let { todo ->
                        viewModel.updateDueDate(todo.id, newDate)
                        Toast.makeText(context, "Due date updated!", Toast.LENGTH_SHORT).show()
                    }
                    editingDueDateItem = null
                }
            )
        }

        if (editingReminderItem != null) {
            EditReminderDialog(
                currentReminder = editingReminderItem?.reminderTime,
                taskId = editingReminderItem?.id ?: "",
                taskTitle = editingReminderItem?.title ?: "",
                onDismiss = { editingReminderItem = null },
                onUpdateReminder = { id, newReminderTime ->
                    viewModel.updateReminder(id, newReminderTime)
                    Toast.makeText(context, "Reminder updated!", Toast.LENGTH_SHORT).show()
                    editingReminderItem = null
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