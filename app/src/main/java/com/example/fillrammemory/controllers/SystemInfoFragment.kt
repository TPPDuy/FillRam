package com.example.fillrammemory.controllers

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
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.fillrammemory.Controllers.CustomSizeDialog
import com.example.fillrammemory.broadcast.MemoryInfoBroadcast
import com.example.fillrammemory.classes.Memory
import com.example.fillrammemory.R
import com.example.fillrammemory.services.MemoryService
import com.example.fillrammemory.utils.Constants
import com.example.fillrammemory.utils.MemoryUtils
import com.example.fillrammemory.viewModels.MemoryInfoViewModel
import kotlinx.android.synthetic.main.fragment_system_info.*

class SystemInfoFragment : Fragment(), View.OnClickListener, CustomSizeDialog.DialogListener {
    private lateinit var systemBroadcast: MemoryInfoBroadcast
    private val viewModel: MemoryInfoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_system_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        systemBroadcast = MemoryInfoBroadcast(viewModel)
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.SYSTEM_INFO)
        intentFilter.addAction(Constants.CREATED_VAR)
        activity?.registerReceiver(systemBroadcast, intentFilter)

        viewModel.getSystemMemoryInfo().observe(viewLifecycleOwner, Observer<Memory>{ mem ->
            run {
                totalValue.text = MemoryUtils.formatToString(mem.total)
                availableValue.text = MemoryUtils.formatToString(mem.available)
                usedValue.text =
                    MemoryUtils.formatToString(mem.total.minus(mem.available))
                progressBar.progress = mem.availablePercent
                progressPercentage.text = "${mem.availablePercent}%"
            }

        })
    }
    override fun onStart() {
        super.onStart()
        btn1.setOnClickListener(this)
        btn100.setOnClickListener(this)
        btn200.setOnClickListener(this)
        btn400.setOnClickListener(this)
        btn500.setOnClickListener(this)
        btn700.setOnClickListener(this)
        btnCustom.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
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
            R.id.btnCustom -> {
                showCustomSizeDialog()
            }
        }
    }

    private fun handleIncreaseMem(value: Int, unit: String) {
        if(MemoryUtils.getInstance(requireContext()).isAvailableAdded(value, unit)) {
            val intent = Intent(requireContext(), MemoryService::class.java)
            intent.putExtra(Constants.WORK_TYPE, Constants.GEN_VAR_JOB)
            intent.putExtra(Constants.MSG_VALUE, value)
            intent.putExtra(Constants.MSG_UNIT, unit)
            MemoryService.enqueueWork(requireContext(), intent)
        } else {
            Toast.makeText(requireContext(), "The value you need to add is more than the current memory value!", Toast.LENGTH_LONG).show()
        }
    }

    private fun showCustomSizeDialog() {
        val customSizeDialog = CustomSizeDialog.newInstance();
        customSizeDialog.setTargetFragment(this, CustomSizeDialog.TARGET)
        customSizeDialog.show(requireFragmentManager(), CustomSizeDialog.TAG)
    }

//    inner class SystemInfoBroadcast : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if(intent != null && intent.action != null) {
//                val action = intent.action
//                if (action.equals(Constants.SYSTEM_INFO)){
//                    Log.d("RECEIVE BROADCAST", intent.toString())
//                    val memoryInfo = intent.extras?.getBundle(Constants.DATA)?.get(Constants.BUNDLE) as Memory
//                    totalValue.text = MemoryUtils.formatToString(memoryInfo.total)
//                    availableValue.text = MemoryUtils.formatToString(memoryInfo.available)
//                    usedValue.text = MemoryUtils.formatToString(memoryInfo.total.minus(memoryInfo.available))
//                    progressBar.progress = memoryInfo.availablePercent
//                    progressPercentage.text = "${memoryInfo.availablePercent}%"
//                }
//            }
//        }
//    }

    override fun onFinishDialog(value: Int, unit: String) {

        val info = "You just added ${value} ${unit}"
//        val toast: Toast = Toast(requireContext())
//        toast.setGravity(Gravity.BOTTOM, 0, 0)
//        toast.duration = Toast.LENGTH_LONG
//        toast.setText(info)
//        toast.show()
        Toast.makeText(requireContext(), info, Toast.LENGTH_LONG).show()
        handleIncreaseMem(value, unit)
    }
}