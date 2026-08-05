package com.example.todolist

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class TodoViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private var todoCollection: com.google.firebase.firestore.CollectionReference? = null

    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    init {
        val user = auth.currentUser
        if (user != null) {
            todoCollection = db.collection("users")
                .document(user.uid)
                .collection("todos")

            todoCollection?.addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("TodoVM", "Listener error", e)
                    return@addSnapshotListener
                }
                _todos.value = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(Todo::class.java)?.apply { id = doc.id }
                } ?: emptyList()
            }
        } else {
            Log.e("TodoVM", "User not logged in – operations will be no-ops")
        }
    }

    fun addTodo(title: String) {
        todoCollection?.add(Todo(title = title, createdAt = Date()))
    }

    fun deleteTodo(id: String) {
        todoCollection?.document(id)?.delete()
    }

    fun editTodo(id: String, newTitle: String) {
        todoCollection?.document(id)?.update("title", newTitle)
    }

    fun toggleDone(id: String, currentDone: Boolean) {
        todoCollection?.document(id)?.update("isDone", !currentDone)
    }
}