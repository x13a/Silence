package me.lucky.silence.text

import android.app.job.JobParameters
import android.app.job.JobService

import me.lucky.silence.AppDatabase

class CleanJobService : JobService() {
    companion object {
        const val JOB_ID = 1000
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        AppDatabase.getInstance(this).allowNumberDao().deleteExpired()
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}