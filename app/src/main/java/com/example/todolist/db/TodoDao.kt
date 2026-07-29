package com.example.todolist.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.todolist.Todo

@Dao
interface TodoDao {
    @Query("SELECT * FROM TODO")
    fun getAllTodo(): LiveData<List<Todo>>

    @Insert
    fun addTodo(todo: Todo)

    @Query("DELETE FROM TODO where id = :id")
    fun deleteTodo(id: Int)

    @Query("UPDATE Todo SET title = :newTitle WHERE id = :id")
    fun updateTitle(id: Int, newTitle: String)


    @Query("UPDATE Todo SET isDone = :done WHERE id = :id")
    fun updateDone(id: Int, done: Boolean)
}