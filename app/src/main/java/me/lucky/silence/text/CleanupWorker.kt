package me.lucky.silence.text

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import me.lucky.silence.AppDatabase

class CleanupWorker(val ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        AppDatabase.getInstance(ctx).allowNumberDao().deleteExpired()
        return Result.success()
    }
}