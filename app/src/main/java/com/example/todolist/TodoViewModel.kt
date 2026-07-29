package com.example.todolist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date

class TodoViewModel : ViewModel() {
    val todoDao = MainApplication.todoDatabase.getTodoDao()
    val todoList: LiveData<List<Todo>> = todoDao.getAllTodo()

    fun addTodo(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.addTodo(Todo(title = title, createdAt = Date.from(Instant.now())))
        }
    }

    fun deleteTodo(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.deleteTodo(id)
        }
    }

    fun editTodo(id: Int, newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.updateTitle(id, newTitle)
        }
    }

    fun toggleDone(id: Int, currentDone: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDao.updateDone(id, !currentDone)
        }
    }
}