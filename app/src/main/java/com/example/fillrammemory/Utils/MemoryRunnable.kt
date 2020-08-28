package com.example.fillrammemory.Utils

import android.content.Context
import com.example.fillrammemory.Classes.Memory

class MemoryRunnable (private var getMemoryThread: GetMemoryThread, private var context: Context) : Runnable {

    private var thread: Thread = Thread(this)
    private var alive: Boolean = false
    override fun run() {

        alive = true

        while (alive) {
            val mem = MemoryUtils.getInstance(context);
            mem.updateMemInfo()
            val memoryInfo = Memory(mem.getTotalRam(), mem.getAvailableRam(), mem.getAvailableMemInPercentage())

            try {
                Thread.sleep(500)
            } catch (exception: InterruptedException) {
                exception.printStackTrace()
            }
            // getMemoryThread.sendMemoryInfo(memoryInfo)
        }
    }

    fun start() {
        if (!thread.isAlive)
            thread.start()
    }

    fun stop() {
        alive = false
    }

}