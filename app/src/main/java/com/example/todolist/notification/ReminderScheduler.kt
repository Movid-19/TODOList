package com.example.todolist.notification

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val TAG_PREFIX = "reminder_"

    fun schedule(context: Context, taskId: String, title: String, reminderTimeMillis: Long) {
        val now = System.currentTimeMillis()
        val delay = reminderTimeMillis - now

        if (delay <= 0) return

        val workRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "task_title" to title,
                    "task_body" to "Reminder for: $title"
                )
            )
            .addTag("$TAG_PREFIX$taskId")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun cancel(context: Context, taskId: String) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("$TAG_PREFIX$taskId")
    }
}