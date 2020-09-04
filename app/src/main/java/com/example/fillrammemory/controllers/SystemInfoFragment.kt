package com.example.fillrammemory.controllers
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.fillrammemory.broadcast.MemoryInfoBroadcast
import com.example.fillrammemory.classes.Memory
import com.example.fillrammemory.R
import com.example.fillrammemory.services.MemoryService
import com.example.fillrammemory.utils.Constants
import com.example.fillrammemory.utils.MemoryUtils
import com.example.fillrammemory.viewModels.MemoryInfoViewModel
import kotlinx.android.synthetic.main.fragment_system_info.*

class SystemInfoFragment : Fragment(), View.OnClickListener, CustomSizeDialog.DialogListener, ServiceConnection {
    private lateinit var systemBroadcast: MemoryInfoBroadcast
    private val viewModel: MemoryInfoViewModel by activityViewModels()
    private var memoryService: MemoryService? = null
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

        val intent = Intent(requireContext(), MemoryService::class.java)
        MemoryService.startServiceExecute(requireContext(), intent) //keep service run independently
        activity?.bindService(intent, this, Context.BIND_IMPORTANT)

        viewModel.getMemoryInfo().observe(viewLifecycleOwner, Observer<Memory>{ mem ->
            run {
                totalValue.text = MemoryUtils.formatToString(mem.total)
                availableValue.text = MemoryUtils.formatToString(mem.available)
                usedValue.text =
                    MemoryUtils.formatToString(mem.total.minus(mem.available))
                createdValue.text =
                    MemoryUtils.formatToString(mem.created)
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
        requireContext().unregisterReceiver(systemBroadcast)
        requireContext().unbindService(this)
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
                showDialog()
            }
        }
    }

    private fun handleIncreaseMem(value: Long, unit: String) =
        if(MemoryUtils.getInstance(requireContext()).isAvailableAdded(value, unit)) {
            /*val intent = Intent(requireContext(), MemoryService::class.java).apply {
                putExtra(Constants.WORK_TYPE, Constants.GEN_VAR_JOB)
                putExtra(Constants.MSG_VALUE, value)
                putExtra(Constants.MSG_UNIT, unit)
            }
            MemoryService.startServiceExecute(requireContext(), intent)*/
            Log.d("INCREASE", if(memoryService == null) "null" else memoryService.toString())
            memoryService?.allocateVariable(value, unit)
        } else {
            Toast.makeText(requireContext(), "The value you need to add is more than the current memory value!", Toast.LENGTH_LONG).show()
        }

    private fun showDialog() {
        val customSizeDialog = CustomSizeDialog.newInstance();
        customSizeDialog.setTargetFragment(this, CustomSizeDialog.TARGET)
        fragmentManager?.let { customSizeDialog.show(it, CustomSizeDialog.TAG) }
    }


    override fun onFinishDialog(value: Long, unit: String) {
        val info = "You just added ${value} ${unit}"
        Toast.makeText(requireContext(), info, Toast.LENGTH_LONG).show()
        handleIncreaseMem(value, unit)
    }

    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
        Log.d("CONNECTION", "Service connected")
        val serviceBinder = binder as MemoryService.ServiceBinder
        memoryService = serviceBinder.serviceInstance
    }

    override fun onServiceDisconnected(componentName: ComponentName?) {
    }
}