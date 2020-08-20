package com.example.fillrammemory.Services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.example.fillrammemory.Utils.MemoryUtils

class MemoryService : JobIntentService() {
    private lateinit var memoryUtils: MemoryUtils;

     override fun onCreate() {
         super.onCreate()
         Log.d(TAG, "Service Execution Started... ")
    }

    override fun onHandleWork(intent: Intent) {
        val value = intent.getIntExtra("value", -1)
        Log.d(TAG, "Running Service, add number: ")
        MemoryUtils.getInstance(this).increaseMemory(value)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Execution Finish... ")
    }

    companion object {
        val JOB_ID = 2
        val TAG = "TAG"
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, MemoryService::class.java, JOB_ID, intent)
        }
    }
}