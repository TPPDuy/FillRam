package com.example.fillrammemory.Utils

import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import com.example.fillrammemory.Services.MemoryService
import com.example.fillrammemory.model.Memory

class GetMemoryThread(threadName: String, private var mUiHandler: Handler) : HandlerThread(threadName){

   private var mHandler: Handler?=null

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        mHandler = getHandler(looper)
    }

    fun sendMemoryInfo(memoryInfo: Memory) {
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


    }


}