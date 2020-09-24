package com.example.fillrammemory.controllers
import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.fillrammemory.broadcast.MemoryInfoBroadcast
import com.example.fillrammemory.classes.Memory
import com.example.fillrammemory.R
import com.example.fillrammemory.services.MemoryService
import com.example.fillrammemory.utils.Constants
import com.example.fillrammemory.utils.MemoryUtils
import com.example.fillrammemory.viewModels.MemoryInfoViewModel
import kotlinx.android.synthetic.main.fragment_gen_var.*
import android.widget.Toast.makeText as makeText1

class GenVarFragment : Fragment(), View.OnClickListener, CustomSizeDialog.DialogListener, ServiceConnection {

    private lateinit var systemBroadcast: MemoryInfoBroadcast
    private val viewModel: MemoryInfoViewModel by activityViewModels()
    private var memoryService: MemoryService? = null
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gen_var, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(requireContext())

        //register broadcast to update system memory info
        systemBroadcast = MemoryInfoBroadcast(viewModel)
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.SYSTEM_INFO)
        intentFilter.addAction(Constants.UPDATE_STATE)
        activity?.registerReceiver(systemBroadcast, intentFilter)

        //bind foreground service
        val intent = Intent(requireContext(), MemoryService::class.java)
        MemoryService.startServiceExecute(requireContext(), intent) //keep service run independently
        activity?.bindService(intent, this, Context.BIND_IMPORTANT)

        //observe ViewModel to update UI
        viewModel.getMemoryInfo().observe(viewLifecycleOwner, Observer<Memory>{ mem ->
            run {
                totalValue.text = MemoryUtils.formatToString(mem.total)
                availableValue.text = MemoryUtils.formatToString(mem.available)
                usedValue.text = MemoryUtils.formatToString(mem.total.minus(mem.available))
                createdValue.text = MemoryUtils.formatToString(mem.created)
                progressBar.progress = mem.availablePercent
                progressPercentage.text = "${mem.availablePercent}%"
            }
        })

        viewModel.getUpdateMemoryState().observe(viewLifecycleOwner, Observer { state ->
            Log.d("Fragment", "Update progress state")
            if (state) progressDialog.show()
            else progressDialog.dismiss()
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
        //btnDeallocate.setOnClickListener(this)
        //change status bar color
        activity?.window?.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.colorPrimaryDark)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(systemBroadcast)
        requireContext().unbindService(this)
    }


    override fun onClick(view: View?) {
        if(!MemoryService.isRunning){
            val intent = Intent(requireContext(), MemoryService::class.java)
            MemoryService.startServiceExecute(requireContext(), intent) //keep service run independently
        }

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
           /* R.id.btnDeallocate -> {
                memoryService?.freeAllocatedVariable()
            }*/
        }
    }

    private fun handleIncreaseMem(value: Long, unit: String) =
        if(MemoryUtils.getInstance(requireContext()).isAvailableAdded(value, unit)) {
            if (MemoryService.isRunning)
                memoryService?.allocateVariable(value, unit)
            else
                makeText1(requireContext(), "Wait until Service run again!", Toast.LENGTH_SHORT).show()
        } else {
            makeText1(requireContext(), "The value you need to add is more than the current memory value!", Toast.LENGTH_LONG).show()
        }

    private fun showDialog() {
        val customSizeDialog = CustomSizeDialog.newInstance();
        customSizeDialog.setTargetFragment(this, CustomSizeDialog.TARGET)
        parentFragmentManager.let { customSizeDialog.show(it, CustomSizeDialog.TAG) }
    }

    override fun onFinishDialog(value: Long, unit: String) {
        val info = "You just added ${value} ${unit}"
        makeText1(requireContext(), info, Toast.LENGTH_LONG).show()
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