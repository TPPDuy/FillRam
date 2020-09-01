package com.example.fillrammemory.Controllers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fillrammemory.Classes.Memory
import com.example.fillrammemory.R
import com.example.fillrammemory.Services.MemoryService
import com.example.fillrammemory.Utils.Constants
import com.example.fillrammemory.Utils.MemoryUtils
import kotlinx.android.synthetic.main.fragment_system_info.*


class SystemInfoFragment : Fragment(), View.OnClickListener {
    private lateinit var systemBroadcast: SystemInfoBroadcast
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        systemBroadcast = SystemInfoBroadcast()
        return inflater.inflate(R.layout.fragment_system_info, container, false)
    }

    override fun onStart() {
        super.onStart()
        btn1.setOnClickListener(this)
        btn100.setOnClickListener(this)
        btn200.setOnClickListener(this)
        btn400.setOnClickListener(this)
        btn500.setOnClickListener(this)
        btn700.setOnClickListener(this)
    }
    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(systemBroadcast, IntentFilter(Constants.SYSTEM_INFO))
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(systemBroadcast)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btn100 -> {
                handleIncreaseMem(100, "MB")
                return
            }
            R.id.btn200 -> {
                handleIncreaseMem(200, "MB")
                return
            }
            R.id.btn400 -> {
                handleIncreaseMem(400, "MB")
                return
            }
            R.id.btn500 -> {
                handleIncreaseMem(500, "MB")
                return
            }
            R.id.btn700 -> {
                handleIncreaseMem(700, "MB")
                return
            }
            R.id.btn1 -> {
                handleIncreaseMem(1, "GB")
                return
            }
        }
    }
    private fun handleIncreaseMem(value: Int, unit: String) {
        val intent = Intent(requireContext(), MemoryService::class.java)
        intent.putExtra(Constants.WORK_TYPE, Constants.GEN_VAR_JOB)
        intent.putExtra(Constants.MSG_VALUE, value)
        intent.putExtra(Constants.MSG_UNIT, unit)
        MemoryService.enqueueWork(requireContext(), intent)
    }
    inner class SystemInfoBroadcast : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent != null && intent.action != null) {
                val action = intent.action
                if (action.equals(Constants.SYSTEM_INFO)){
                    Log.d("RECEIVE BROADCAST", intent.toString())
                    val memoryInfo = intent.extras?.getBundle(Constants.DATA)?.get(Constants.BUNDLE) as Memory
                    totalValue.text = MemoryUtils.formatToString(memoryInfo.total)
                    availableValue.text = MemoryUtils.formatToString(memoryInfo.available)
                    usedValue.text = MemoryUtils.formatToString(memoryInfo.total.minus(memoryInfo.available))
                    progressBar.progress = memoryInfo.availablePercent
                    progressPercentage.text = "${memoryInfo.availablePercent}%"
                }
            }
        }
    }
}