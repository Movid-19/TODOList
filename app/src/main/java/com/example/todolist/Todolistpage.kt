package com.example.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun Todolistpage(viewModel: TodoViewModel) {
    val todolist by viewModel.todoList.observeAsState()
    var inputText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<Todo?>(null) }
    var editText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxHeight().padding(8.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
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


        todolist?.let { list ->
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

@Composable
fun Todoitem(item: Todo, onDelete: () -> Unit, onEdit: () -> Unit, onToggleDone: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Checkbox(modifier = Modifier.background(color = Color.Black),
            checked = item.isDone,
            onCheckedChange = { onToggleDone() }
        )

        Spacer(modifier = Modifier.width(2.dp))

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
                textDecoration = if (item.isDone) TextDecoration.LineThrough else null
            )
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