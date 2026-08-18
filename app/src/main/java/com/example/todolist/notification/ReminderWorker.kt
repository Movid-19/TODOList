package com.example.todolist.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString("task_title") ?: "Task Reminder"
        val body = inputData.getString("task_body") ?: "Don't forget your task!"

        NotificationHelper.showNotification(applicationContext, title, body)

        return Result.success()
    }
}