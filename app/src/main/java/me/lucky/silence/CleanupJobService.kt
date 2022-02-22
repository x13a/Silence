package me.lucky.silence

import android.app.job.JobParameters
import android.app.job.JobService

class CleanupJobService : JobService() {
    companion object {
        const val JOB_ID = 1000
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        AppDatabase.getInstance(this).allowNumberDao().deleteInactive()
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}
