package com.example.fillrammemory.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.fillrammemory.classes.Memory
import com.example.fillrammemory.utils.Constants
import com.example.fillrammemory.viewModels.MemoryInfoViewModel

class MemoryInfoBroadcast(var viewModel: MemoryInfoViewModel? = null): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null && intent.action != null) {
            val action = intent.action
            if (action.equals(Constants.SYSTEM_INFO)){
                val memoryInfo = intent.extras?.getBundle(Constants.DATA)?.get(Constants.BUNDLE) as Memory?
                if (memoryInfo != null) {
                    viewModel?.updateMemInfo(memoryInfo)
                }
            } else if (action.equals(Constants.CREATED_VAR)){
                val buffSize = intent.extras?.getLong(Constants.DATA)
                if (buffSize != null) {
                    viewModel?.updateCreatedMem(buffSize)
                    Log.d("BROADCAST", "update created $buffSize")
                }
            }
        }
    }
}