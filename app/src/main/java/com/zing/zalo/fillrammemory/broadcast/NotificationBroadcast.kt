package com.zing.zalo.fillrammemory.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.zing.zalo.fillrammemory.services.MemoryService

class NotificationBroadcast: BroadcastReceiver() {
    override fun onReceive(p0: Context, p1: Intent?) {

        val action = p1?.action ?: ""
        if (action == "FREE_ACTION") {
            Log.d("Broadcast", "Button Free Clicked")
            MemoryService.getInstance().handleFreeAllAllocated()
        } else if (action == "STOP_ACTION") {
            Log.d("Broadcast", "Button Stop Clicked")
            MemoryService.stopService(p0)
        }
        val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        p0.sendBroadcast(closeIntent)
    }
}