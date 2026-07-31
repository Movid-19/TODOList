package com.example.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class) //using this to supress the warning
@Composable
fun Todolistpage(viewModel: TodoViewModel) {
    val allTodos by viewModel.todoList.observeAsState()// this allows me to manage the todos
    var inputText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<Todo?>(null) }
    var editText by remember { mutableStateOf("") }
    val visibleTodos = allTodos?.filter { !it.isDone }
    //allows to add the top app bar, appbar on the top, app content in the middle
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TODO List") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(paddingValues)
                .padding(8.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = inputText,
                    onValueChange = { inputText = it }
                )
                Button(onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.addTodo(inputText)
                        inputText = ""
                    }
                }) {
                    Text(text = "Add")
                }
            }
//this creates a lazy list for the todos
            visibleTodos?.let { list ->
                LazyColumn {
                    itemsIndexed(list) { _, item ->
                        Todoitem(
                            item = item,
                            onDelete = { viewModel.deleteTodo(item.id) },
                            onEdit = {
                                editingItem = item
                                editText = item.title
                            },
                            onToggleDone = { viewModel.toggleDone(item.id, item.isDone) }
                        )
                    }
                }
            } ?: Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "No Todo",
                fontSize = 16.sp
            )

            if (editingItem != null) {
                AlertDialog(
                    onDismissRequest = { editingItem = null },
                    title = { Text("Edit Task") },
                    text = {
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            label = { Text("Title") }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                editingItem?.let {
                                    if (editText.isNotBlank()) {
                                        viewModel.editTodo(it.id, editText)
                                    }
                                }
                                editingItem = null
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { editingItem = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun Todoitem(item: Todo, onDelete: () -> Unit, onEdit: () -> Unit, onToggleDone: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // menu icon (three dots)
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = Color.Black
            )
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
        }//this is the drop down menu button, setting the parameter for the onclick


        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = SimpleDateFormat("HH:mm:aa, dd/MM", Locale.ENGLISH).format(item.createdAt),
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )
            Text(
                text = item.title,
                fontSize = 20.sp,
                color = Color.Black,
            )/* removed the cutthrough line because we are removing the TO,DO entirely
            also removed the Checkbox because we didnt need it anymore due to the drop down button we added */
        }


        IconButton(onClick = onEdit) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_edit_24),
                contentDescription = "Edit",
                tint = Color.Gray
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_delete_24),
                contentDescription = "Delete",
                tint = Color.Gray
            )
        }
    }
}