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
            if (intent.action == Constants.SYSTEM_INFO){
                val memoryInfo = intent.extras?.getBundle(Constants.DATA)?.get(Constants.BUNDLE) as Memory?
                if (memoryInfo != null) {
                    viewModel?.updateMemInfo(memoryInfo)
                }
            }
            if (intent.action == Constants.UPDATE_STATE){
                viewModel?.updateState(intent.extras?.getBoolean(Constants.DATA) ?: false)
            }
        }
    }
}