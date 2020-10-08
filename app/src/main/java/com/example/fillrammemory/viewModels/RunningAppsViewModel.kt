package com.example.fillrammemory.viewModels

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.os.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.example.fillrammemory.classes.AppInfo
import com.example.fillrammemory.utils.Constants
import com.example.fillrammemory.utils.MemoryUtils
import com.example.fillrammemory.utils.Utils
import java.util.concurrent.Executors

class RunningAppsViewModel(application: Application): AndroidViewModel(application), Runnable, Handler.Callback {
    private val runningApps: MutableLiveData<ArrayList<AppInfo>> = MutableLiveData()
    private val mainHandler: Handler = Handler(Looper.getMainLooper(), this)
    private val threadPool = Executors.newFixedThreadPool(1)
    private val context = getApplication<Application>().applicationContext

    init{
        if (Utils.checkPermission(context))
            retrieveRunningApps()
    }

    fun retrieveRunningApps(){
        threadPool.execute(this)
    }
    fun getRunningApps(): MutableLiveData<ArrayList<AppInfo>>{
        return runningApps
    }
    override fun run() {
        val result = (
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) MemoryUtils.getInstance(
                    context
                ).getRunningAppPostLollipop()
                else MemoryUtils.getInstance(context).getRunningAppPreLollipop()
                )
        val msg = mainHandler.obtainMessage(1)
        val bundle = Bundle().apply {
            putSerializable(Constants.DATA, result)
        }
        msg.data = bundle
        mainHandler.sendMessageAtFrontOfQueue(msg)
    }
    override fun handleMessage(msg: Message): Boolean {
        if(msg.what == 1){
            val listApp = (msg.data as Bundle).get(Constants.DATA) as ArrayList<*>
            runningApps.value = listApp as ArrayList<AppInfo>
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        threadPool.shutdown()
    }


}