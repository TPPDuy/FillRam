package com.example.fillrammemory.Services

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.TextView
import androidx.core.app.JobIntentService
import com.example.fillrammemory.Utils.Constants
import com.example.fillrammemory.Utils.MemoryUtils
import java.nio.ByteBuffer

class MemoryService : JobIntentService() {
     override fun onCreate() {
         super.onCreate()
         Log.d(TAG, "Service Execution Started... ")
    }

    override fun onHandleWork(intent: Intent) {
        val value = intent.getIntExtra(Constants.MSG_VALUE, 0)
        val unit = intent.getStringExtra(Constants.MSG_UNIT)
        val bytesValue =  MemoryUtils.getInstance(this).convertValueToBytes(value, unit);

        val arr = varGenerator(bytesValue)
        if (arr != null) {
            mAllocations.add(arr)
        }
        val dataSize = arr?.remaining()?.div((1024*1024))
        Log.d(TAG, "Running Service, increase  ${dataSize} size of Ram ")
        Log.d(TAG, "Allocation ${mAllocations.size} bytes array")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Execution Finish... ")
    }

    private external fun varGenerator(size: Long): ByteBuffer?

    companion object {
        const val JOB_ID = 2
        var TAG = MemoryService::class.simpleName ?: "MEMORY SERVICE"
        private var mAllocations: ArrayList<ByteBuffer> = ArrayList()
        init {
            try{
                System.loadLibrary("nativeLib")
            } catch(e: UnsatisfiedLinkError){
                e.printStackTrace()
            }
        }
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, MemoryService::class.java, JOB_ID, intent)
        }
    }
}