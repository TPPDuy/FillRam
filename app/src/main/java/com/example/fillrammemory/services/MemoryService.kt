package com.example.fillrammemory.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.example.fillrammemory.utils.Constants
import com.example.fillrammemory.utils.MemoryUtils
import java.nio.ByteBuffer

class MemoryService : JobIntentService() {

     override fun onCreate() {
         super.onCreate()
         Log.d(TAG, "Service Execution Started... ")
    }

    override fun onHandleWork(intent: Intent) {
        val workType = intent.getStringExtra(Constants.WORK_TYPE)
        if (workType != null) {
            if (workType == Constants.GEN_VAR_JOB) {
                val value = intent.getIntExtra(Constants.MSG_VALUE, 0)
                val unit = intent.getStringExtra(Constants.MSG_UNIT)
                val bytesValue =  MemoryUtils.getInstance(this).convertValueToBytes(value, unit ?: "MB");

                val arr = varGenerator(bytesValue)
                if (arr != null) {
                    mAllocations.add(arr)
                    mAllocationSize += arr.capacity()
                    Log.d("ARR SIZE", arr.capacity().toString())
                    Log.d("SIZE", mAllocationSize.toString())
                    val broadcastIntent = Intent()
                    broadcastIntent.action = Constants.CREATED_VAR
                    broadcastIntent.putExtra(Constants.DATA, mAllocationSize)
                    sendBroadcast(broadcastIntent)
                }
            } else if (workType == Constants.FREE_MEM_JOB){
                for (buff in mAllocations){
                    freeVar(buff)
                    mAllocationSize = 0
                    val broadcastIntent = Intent()
                    broadcastIntent.action = Constants.CREATED_VAR
                    broadcastIntent.putExtra(Constants.DATA, mAllocationSize)
                    sendBroadcast(broadcastIntent)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Execution Finish... ")
    }

    companion object {
        private const val JOB_ID = 2
        var TAG = MemoryService::class.simpleName ?: "MEMORY SERVICE"
        private var mAllocations: ArrayList<ByteBuffer> = ArrayList()
        private var mAllocationSize: Long = 0
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

    private external fun varGenerator(size: Long): ByteBuffer?
    private external fun freeVar(buff: ByteBuffer)
}