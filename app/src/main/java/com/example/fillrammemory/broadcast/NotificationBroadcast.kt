package com.example.fillrammemory.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.fillrammemory.R
import com.example.fillrammemory.services.MemoryService
import com.example.fillrammemory.utils.Constants

class NotificationBroadcast: BroadcastReceiver() {
    override fun onReceive(p0: Context, p1: Intent?) {
        Log.d("Broadcast", "Receive intent")
        when(p1?.getIntExtra(Constants.BUTTON_CLICKED, 0)){
            R.id.btnFree -> {
                Log.d("Broadcast", "Button Free Clicked")
                MemoryService.getInstance().freeAllocatedVariable()
            }
            R.id.btnStop -> {
                Log.d("Broadcast", "Button Stop Clicked")
                MemoryService.stopService(p0)
            }
        }
        val closeIntent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        p0.sendBroadcast(closeIntent)
    }
}