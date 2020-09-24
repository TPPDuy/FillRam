package com.example.fillrammemory.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fillrammemory.classes.Memory

class MemoryInfoViewModel: ViewModel() {
    private var memoryInfo = MutableLiveData<Memory>(Memory())
    private var isUpdateMemory = MutableLiveData<Boolean>(false)

    fun updateMemInfo(info: Memory){
        memoryInfo.value = info
    }

    fun getMemoryInfo(): LiveData<Memory>{
        return memoryInfo
    }

    fun updateState(state: Boolean){
        isUpdateMemory.value = state
    }

    fun getUpdateMemoryState(): LiveData<Boolean>{
        return isUpdateMemory
    }
}