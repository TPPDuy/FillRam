package com.example.fillrammemory.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fillrammemory.classes.Memory

class MemoryInfoViewModel: ViewModel() {
    private var memoryInfo = MutableLiveData<Memory>(Memory())

    fun updateMemInfo(info: Memory){
        val newInfo = memoryInfo.value?.created?.let {
            Memory(info.total, info.available,
                it, info.availablePercent)
        }
        memoryInfo.value = newInfo
    }
    fun updateCreatedMem(size: Long){
        Log.d("Update created mem: ", size.toString())
        val newInfo = memoryInfo.value
        newInfo?.created = size
        memoryInfo.value = newInfo
    }

    fun getMemoryInfo(): LiveData<Memory>{
        return memoryInfo
    }
}