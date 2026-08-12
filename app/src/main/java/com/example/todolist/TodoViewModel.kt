package com.example.todolist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Date

class TodoViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var todoCollection: com.google.firebase.firestore.CollectionReference? = null

    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos = _todos.asStateFlow()

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType = _filterType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val filteredTodos = combine(_todos, _filterType, _searchQuery) { list, filter, query ->
        list.filter { filterTodo(it, filter, query) }
    }

    val pendingTodos = filteredTodos
        .map { it.filter { !it.isDone } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTodos = filteredTodos
        .map { it.filter { it.isDone } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun filterTodo(todo: Todo, filter: FilterType, query: String): Boolean {
        if (query.isNotEmpty() && !todo.title.contains(query, ignoreCase = true)) return false

        return when (filter) {
            FilterType.ALL -> true
            FilterType.TODAY -> isToday(todo.dueDate)
            FilterType.THIS_WEEK -> isThisWeek(todo.dueDate)
            FilterType.THIS_MONTH -> isThisMonth(todo.dueDate)
            FilterType.OVERDUE -> isOverdue(todo.dueDate)
        }
    }

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
            Log.e("TodoVM", "User not logged in")
        }
    }

    private fun isToday(date: Date?): Boolean {
        if (date == null) return false
        val today = Calendar.getInstance()
        val cal = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
    }

    private fun isThisWeek(date: Date?): Boolean {
        if (date == null) return false
        val today = Calendar.getInstance()
        val cal = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                today.get(Calendar.WEEK_OF_YEAR) == cal.get(Calendar.WEEK_OF_YEAR)
    }

    private fun isThisMonth(date: Date?): Boolean {
        if (date == null) return false
        val today = Calendar.getInstance()
        val cal = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
    }

    private fun isOverdue(date: Date?): Boolean {
        if (date == null) return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return date.before(today.time)
    }

    fun setFilterType(filter: FilterType) {
        _filterType.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTodo(title: String, dueDate: Date?) {
        todoCollection?.add(Todo(title = title, createdAt = Date(), dueDate = dueDate))
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

    fun updateDueDate(id: String, newDate: Date?) {
        todoCollection?.document(id)?.update("dueDate", newDate)
    }
}

enum class FilterType {
    ALL, TODAY, THIS_WEEK, THIS_MONTH, OVERDUE
}