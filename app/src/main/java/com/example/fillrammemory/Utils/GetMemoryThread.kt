package com.example.fillrammemory.Utils

import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message

class GetMemoryThread(threadName: String) : HandlerThread(threadName) {

    lateinit var handler: Handler

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
            }
        }
    }
}