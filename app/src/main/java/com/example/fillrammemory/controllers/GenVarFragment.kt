package com.example.fillrammemory.controllers
import android.annotation.SuppressLint
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.fillrammemory.broadcast.MemoryInfoBroadcast
import com.example.fillrammemory.classes.Memory
import com.example.fillrammemory.R
import com.example.fillrammemory.services.MemoryService
import com.example.fillrammemory.utils.Constants
import com.example.fillrammemory.utils.MemoryUtils
import com.example.fillrammemory.viewModels.MemoryInfoViewModel
import kotlinx.android.synthetic.main.expandable_fab.*
import kotlinx.android.synthetic.main.fragment_gen_var.*
import android.widget.Toast.makeText as makeText1

class GenVarFragment : Fragment(), View.OnClickListener, CustomSizeDialog.DialogListener, ServiceConnection {

    private lateinit var systemBroadcast: MemoryInfoBroadcast
    private val viewModel: MemoryInfoViewModel by activityViewModels()
    private var memoryService: MemoryService? = null
    private lateinit var progressDialog: ProgressDialog

    private lateinit var fabCloseAnim: Animation
    private lateinit var fabOpenAnim: Animation
    private lateinit var fabRotateClockAnim: Animation
    private lateinit var fabRotateAntiClockAnim: Animation
    private var fabExpandState = MutableLiveData<Boolean>(false)


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
        requireActivity().registerReceiver(systemBroadcast, intentFilter)

        //bind foreground service
        val intent = Intent(requireContext(), MemoryService::class.java)
        MemoryService.startServiceExecute(requireContext(), intent) //keep service run independently
        requireActivity().bindService(intent, this, Context.BIND_IMPORTANT)

        initView();
    }

    private fun initView(){
        fabCloseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_close)
        fabOpenAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_open)
        fabRotateClockAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_rotate_clock)
        fabRotateAntiClockAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_rotate_anticlock)
        btn1.setOnClickListener(this)
        btn100.setOnClickListener(this)
        btn200.setOnClickListener(this)
        btn400.setOnClickListener(this)
        btn500.setOnClickListener(this)
        btn700.setOnClickListener(this)
        btnCustom.setOnClickListener(this)
        btnDeallocate.setOnClickListener(this)
        overlayLayout.setOnClickListener(this)
        btnDeallocateAll.setOnClickListener(this)
        btnDeallocateCustom.setOnClickListener(this)
        //change status bar color
        activity?.window?.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.colorPrimary)

        //observe ViewModel to update UI
        viewModel.getMemoryInfo().observe(viewLifecycleOwner, Observer<Memory>{ mem ->
            run {
                totalValue.text = MemoryUtils.formatToString(mem.total.toLong())
                availableValue.text = MemoryUtils.formatToString(mem.available.toLong())
                //usedValue.text = MemoryUtils.formatToString(mem.total.minus(mem.available).toLong())
                createdValue.text = MemoryUtils.formatToString(mem.created.toLong())
                progressBar.progress = mem.availablePercent
                progressPercentage.text = "${mem.availablePercent}%"
            }
        })

        viewModel.getUpdateMemoryState().observe(viewLifecycleOwner, Observer { state ->
            Log.d("Fragment", "Update progress state")
            if (state) {
                progressDialog.show()
                MemoryService.setAllocateState(true)
            }
            else {
                progressDialog.dismiss()
                MemoryService.setAllocateState(false)
            }
        })

        fabExpandState.observe(viewLifecycleOwner, Observer { state ->
            handleFabExpandState(state)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(systemBroadcast)
        //requireContext().unbindService(this)
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
            R.id.btnDeallocate -> {
                fabExpandState.value = !(fabExpandState.value ?: false)
            }
            R.id.btnDeallocateAll -> {
                fabExpandState.value = false
                memoryService?.handleFreeAllocated()
            }
            R.id.btnDeallocateCustom -> {
                Log.e("CLICK BUTTON", "Deallocate by chunk")
            }
            R.id.overlayLayout -> {
                fabExpandState.value = false
            }
            else -> {
                Log.e("CLICK BUTTON", "Button is undefined")
            }
        }
    }

    private fun handleIncreaseMem(value: Long, unit: String) =
        if(MemoryUtils.getInstance(requireContext()).isAvailableAdded(value, unit)) {
            if (MemoryService.isRunning)
                memoryService?.handleFillRam(value, unit)
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

    private fun handleFabExpandState(isOpen: Boolean){
        if (!isOpen){
            text_custom.visibility = View.INVISIBLE
            text_free_all.visibility = View.INVISIBLE
            overlayLayout.setBackgroundColor(Color.TRANSPARENT)
            mainLayout.isClickable = true
            btnDeallocateAll.run {
                startAnimation(fabCloseAnim)
                isClickable = false
            }
            btnDeallocateCustom.run{
                startAnimation(fabCloseAnim)
                isClickable = false
            }
        } else{
            text_custom.visibility = View.VISIBLE
            text_free_all.visibility = View.VISIBLE
            overlayLayout.setBackgroundColor(Color.argb(80, 0, 0, 0))
            overlayLayout.requestFocus()
            overlayLayout.isClickable = true
            mainLayout.isClickable = false
            btnDeallocateAll.run {
                startAnimation(fabOpenAnim)
                isClickable = true
            }
            btnDeallocateCustom.run{
                startAnimation(fabOpenAnim)
                isClickable = true
            }
        }
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