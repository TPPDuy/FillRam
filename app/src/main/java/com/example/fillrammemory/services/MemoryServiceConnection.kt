package com.example.fillrammemory.services

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

class MemoryServiceConnection : ServiceConnection {
    var isBounded = false
    var memoryService: MemoryService? = null

    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
        Log.d("CONNECTION", "Service connected")
        val serviceBinder = binder as MemoryService.ServiceBinder
        memoryService = serviceBinder.serviceInstance
        isBounded = true
    }

    override fun onServiceDisconnected(componentName: ComponentName?) {
        isBounded = false
    }
}