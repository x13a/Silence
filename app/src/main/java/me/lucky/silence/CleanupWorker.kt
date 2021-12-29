package me.lucky.silence

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class CleanupWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams)
{
    private val db by lazy { AppDatabase.getInstance(context).smsFilterDao() }

    override fun doWork(): Result {
        db.deleteInactive()
        return Result.success()
    }
}
