package com.example.fillrammemory.Utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message

class GetMemoryThread(threadName: String) : HandlerThread(threadName) {

    var mainHandler: Handler? = null

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        mainHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {

            }
        }
    }
}