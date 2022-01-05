package me.lucky.silence

import android.app.job.JobParameters
import android.app.job.JobService

class CleanupJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        AppDatabase.getInstance(this).tmpNumberDao().deleteInactive()
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}
