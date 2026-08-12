package com.example.todolist

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class Todo(
    var title: String = "",
    var createdAt: Date = Date(),
    @get:PropertyName("isDone")
    @set:PropertyName("isDone")
    var isDone: Boolean = false,
    var dueDate: Date? = null,
    var id: String = ""
)