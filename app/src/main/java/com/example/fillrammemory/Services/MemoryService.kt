package com.example.fillrammemory.Services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.example.fillrammemory.Utils.MemoryUtils

class MemoryService : JobIntentService() {
     override fun onCreate() {
         super.onCreate()
         Log.d(TAG, "Service Execution Started... ")
         MemoryUtils.getInstance(this).getMemoryInfo()
    }

    override fun onHandleWork(intent: Intent) {
        val value = intent.getStringExtra("value")
        Log.d(TAG, "Running Service, increase size of Ram ")
        MemoryUtils.getInstance(this).increaseMemory(value)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Execution Finish... ")
        MemoryUtils.getInstance(this).getMemoryInfo()
    }

    companion object {
        const val JOB_ID = 2
        var TAG = MemoryService::class.simpleName ?: "MEMORY SERVICE"
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, MemoryService::class.java, JOB_ID, intent)
        }
    }
}