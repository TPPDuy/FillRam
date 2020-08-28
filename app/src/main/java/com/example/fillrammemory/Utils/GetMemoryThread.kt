package com.example.fillrammemory.Utils

import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import com.example.fillrammemory.Classes.Memory

class GetMemoryThread(threadName: String, val context: Context) : HandlerThread(threadName){

    private lateinit var mHandler: Handler
    private val memoryUtils = MemoryUtils.getInstance(context)

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        mHandler = Handler()
        mHandler.post(GetMemInfoRunnable())
    }

    inner class GetMemInfoRunnable : Runnable {
        private lateinit var memoryInfo: Memory
        override fun run() {
            memoryUtils.updateMemInfo()
            memoryInfo = Memory(memoryUtils.getTotalRam(), memoryUtils.getAvailableRam(), memoryUtils.getAvailableMemInPercentage())
            val intent = Intent()
            val bundle = Bundle()
            bundle.putSerializable(Constants.BUNDLE, memoryInfo)
            intent.action = Constants.SYSTEM_INFO
            intent.putExtra(Constants.DATA, bundle)
            context.sendBroadcast(intent)
            Log.d("SEND BROADCAST", intent.toString())
            mHandler.postDelayed(this, 500)
        }
    }
    /*fun sendMemoryInfo(memoryInfo: Memory) {
        val message = Message()
        message.obj = memoryInfo
        mHandler?.sendMessage(message)
    }

    private fun getHandler(looper: Looper?): Handler? {
        return object : Handler(looper){
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val memoryInfo = msg?.obj as Memory
                val processedMessage = Message()
                processedMessage.obj = memoryInfo
                mUiHandler.sendMessage(processedMessage)
            }
        }


    }*/

}