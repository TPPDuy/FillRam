package com.example.fillrammemory.utils

import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import com.example.fillrammemory.classes.Memory
import com.example.fillrammemory.services.MemoryService

class GetMemoryThread(threadName: String, val context: Context) : HandlerThread(threadName){

    private lateinit var mHandler: Handler
    private val memoryUtils = MemoryUtils.getInstance(context)
    private val runnable = GetMemInfoRunnable()

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        mHandler = Handler()
        mHandler.post(runnable)
    }

    override fun quitSafely(): Boolean {
        mHandler.removeCallbacks(runnable)
        return super.quitSafely()
    }

    inner class GetMemInfoRunnable : Runnable {
        private lateinit var memoryInfo: Memory
        override fun run() {
            memoryUtils.updateMemInfo()
            memoryInfo = Memory(
                total = memoryUtils.getTotalRam().toDouble(),
                available = memoryUtils.getAvailableRam().toDouble(),
                created = /*MemoryService.getAllocationSize().toDouble()*/MemoryService.mAllocationSize.toDouble(),
                availablePercent = memoryUtils.getAvailableMemInPercentage()
            )
            val intent = Intent()
            val bundle = Bundle()
            bundle.putSerializable(Constants.BUNDLE, memoryInfo)
            intent.action = Constants.SYSTEM_INFO
            intent.putExtra(Constants.DATA, bundle)
            context.sendBroadcast(intent)
            Log.d("SEND BROADCAST", intent.toString())
            mHandler.postDelayed(this, 1000)
        }
    }
}