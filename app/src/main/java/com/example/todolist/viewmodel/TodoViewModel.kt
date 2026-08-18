package com.example.todolist.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.Todo
import com.example.todolist.notification.ReminderScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date

@OptIn(FlowPreview::class)
class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var todoCollection: CollectionReference? = null

    private val _todos = MutableStateFlow<List<Todo>>(emptyList())

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType = _filterType.asStateFlow()

    private val _searchQueryRaw = MutableStateFlow("")
    val searchQuery = _searchQueryRaw.asStateFlow()

    private val _searchQueryDebounced = _searchQueryRaw
        .debounce(300)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _sortType = MutableStateFlow(SortType.NEWEST_FIRST)
    val sortType = _sortType.asStateFlow()

    private var deletedTask: Todo? = null
    private var deleteJob: Job? = null

    private val filteredTodos = combine(_todos, _filterType, _searchQueryDebounced, _sortType) {
            allTodos, filter, query, sort ->
        allTodos
            .filter { todo ->
                val matchesQuery = query.isEmpty() || todo.title.contains(query, ignoreCase = true)
                matchesQuery && when (filter) {
                    FilterType.ALL -> true
                    FilterType.TODAY -> isToday(todo.dueDate)
                    FilterType.THIS_WEEK -> isThisWeek(todo.dueDate)
                    FilterType.THIS_MONTH -> isThisMonth(todo.dueDate)
                    FilterType.OVERDUE -> isOverdue(todo.dueDate)
                }
            }
            .sortedWith(compareBy { todo ->
                when (sort) {
                    SortType.NEWEST_FIRST -> -todo.createdAt.time
                    SortType.OLDEST_FIRST -> todo.createdAt.time
                    SortType.DUE_DATE -> todo.dueDate?.time ?: Long.MAX_VALUE
                    SortType.ALPHABETICAL -> todo.title.lowercase()
                }
            })
    }

    val pendingTodos = filteredTodos
        .map { it.filter { !it.isDone } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTodos = filteredTodos
        .map { it.filter { it.isDone } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        @Suppress("DEPRECATION")
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

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
        _searchQueryRaw.value = query
    }

    fun setSortType(sort: SortType) {
        _sortType.value = sort
    }

    fun addTodo(title: String, dueDate: Date?, reminderTime: Long?) {
        val newTodo = Todo(
            title = title,
            createdAt = Date(),
            dueDate = dueDate,
            reminderTime = reminderTime
        )
        todoCollection?.add(newTodo)?.addOnSuccessListener { docRef ->
            val id = docRef.id
            reminderTime?.let { time ->
                ReminderScheduler.schedule(getApplication(), id, title, time)
            }
        }
    }

    fun deleteTodoWithUndo(id: String, onUndoAvailable: (Todo) -> Unit) {
        val taskToDelete = _todos.value.find { it.id == id }
        if (taskToDelete == null) {
            deleteTodo(id)
            return
        }

        deletedTask = taskToDelete
        ReminderScheduler.cancel(getApplication(), id)
        todoCollection?.document(id)?.delete()
        onUndoAvailable(taskToDelete)

        deleteJob?.cancel()
        deleteJob = viewModelScope.launch {
            delay(5000)
            deletedTask = null
        }
    }

    fun undoDelete() {
        val task = deletedTask
        if (task != null) {
            todoCollection?.add(task)
            // Restore reminder if it had one
            task.reminderTime?.let { time ->
                ReminderScheduler.schedule(getApplication(), task.id, task.title, time)
            }
            deletedTask = null
            deleteJob?.cancel()
            deleteJob = null
        }
    }

    fun deleteTodo(id: String) {
        ReminderScheduler.cancel(getApplication(), id)
        todoCollection?.document(id)?.delete()
    }

    fun editTodo(id: String, newTitle: String) {
        todoCollection?.document(id)?.update("title", newTitle)
    }

    fun updateReminder(id: String, newReminderTime: Long?) {
        todoCollection?.document(id)?.update("reminderTime", newReminderTime)
            ?.addOnSuccessListener {
                // Cancel old and schedule new
                ReminderScheduler.cancel(getApplication(), id)
                newReminderTime?.let { time ->
                    val task = _todos.value.find { it.id == id }
                    task?.let {
                        ReminderScheduler.schedule(getApplication(), id, it.title, time)
                    }
                }
            }
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

enum class SortType {
    NEWEST_FIRST,
    OLDEST_FIRST,
    DUE_DATE,
    ALPHABETICAL
}