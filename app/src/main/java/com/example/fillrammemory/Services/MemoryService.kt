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
//         for (i in arrayList) {
//             Log.d(TAG, "onHandleWork: The number is: $i")
//         }
    }


    override fun onHandleWork(intent: Intent) {
        val value = intent.getIntExtra("value", -1)
        arrayList.add(value)
//        for (i in 0 until maxCount) {
//            Log.d(TAG, "onHandleWork: The number is: $i")
//            try {
//                Thread.sleep(1000)
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//
//        }
        Log.d(TAG, "Running Service, add number: ")
        for (i in arrayList) {
            Log.d(TAG, "onHandleWork: The number is: $i")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Execution Finish... ")
    }

    companion object {
        val JOB_ID = 2
        val TAG = "TAG"
        val arrayList = ArrayList<Int>()

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, MemoryService::class.java, JOB_ID, intent)
        }
    }
}