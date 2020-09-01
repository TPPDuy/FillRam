package com.example.fillrammemory.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fillrammemory.classes.Memory

class MemoryInfoViewModel: ViewModel() {
    private var systemMemoryInfo = MutableLiveData<Memory>(Memory())
    private var appMemoryInfo = MutableLiveData<Memory>(Memory())

    fun updateSystemMemInfo(newInfo: Memory){
        systemMemoryInfo.value = newInfo
    }
    fun updateAppMemInfo(newInfo: Memory){
        appMemoryInfo.value?.total = newInfo.total
        appMemoryInfo.value?.available = newInfo.available
    }
    fun updateCreatedMem(size: Long){
        Log.d("Update created mem: ", size.toString())
        appMemoryInfo.value?.created = size
    }

    fun getSystemMemoryInfo(): LiveData<Memory>{
        return systemMemoryInfo
    }

    fun getAppMemoryInfo(): LiveData<Memory>{
        return appMemoryInfo
    }

}