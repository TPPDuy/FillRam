package com.zing.zalo.fillrammemory.controllers

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.zing.zalo.fillrammemory.R
import com.zing.zalo.fillrammemory.adapters.MemUsageAppAdapter
import com.zing.zalo.fillrammemory.classes.AppInfo
import com.zing.zalo.fillrammemory.classes.Memory
import com.zing.zalo.fillrammemory.utils.MemoryUtils
import com.zing.zalo.fillrammemory.utils.Utils
import com.zing.zalo.fillrammemory.viewModels.MemoryInfoViewModel
import com.zing.zalo.fillrammemory.viewModels.RunningAppsViewModel
import kotlinx.android.synthetic.main.fragment_memory_info.*

class MemoryInfoFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var memUsageAdapter: MemUsageAppAdapter
    private val viewModel: MemoryInfoViewModel by activityViewModels()
    private val runningAppsViewModel: RunningAppsViewModel by activityViewModels()
    //private var isHigherAverageMem: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_memory_info, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memUsageAdapter = MemUsageAppAdapter(requireContext(), runningAppsViewModel.getRunningApps().value)

        listUsageApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = memUsageAdapter
        }

        viewModel.getMemoryInfo().observe(viewLifecycleOwner, Observer<Memory>{ mem ->
            run {
                val usedValue = mem.total.minus(mem.available)
                val percentage = ((usedValue.times(100)).div(mem.total)).toLong()
                percentageText.text = percentage.toString()
                when {
                    percentage >= 75 -> {
                        activity?.window?.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.colorPrimary)
                        percentageHolder.background = getDrawable(requireContext(), R.drawable.gradient_orange_background)
                    }
                    percentage >= 65 -> {
                        activity?.window?.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.colorHalfBaked)
                        percentageHolder.background = getDrawable(requireContext(), R.drawable.gradient_blue_background)
                    }
                    else -> {
                        activity?.window?.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.colorGreenLight)
                        percentageHolder.background = getDrawable(requireContext(), R.drawable.gradient_green_background)
                    }
                }
            }
        })

        runningAppsViewModel.getRunningApps().observe(viewLifecycleOwner, Observer<ArrayList<AppInfo>>{listApps ->
            run {
                memUsageAdapter.replaceData(listApps)
            }
        })

        memUsageAdapter.getData().observe(viewLifecycleOwner, Observer { data ->
            run {
                swipeRefresh.isRefreshing = false
                if (data.size == 0) {
                    emptyItemHolder?.visibility = View.VISIBLE
                    btnSpeedUp.isEnabled = false
                }
                else {
                    emptyItemHolder?.visibility = View.GONE
                    btnSpeedUp.isEnabled = true
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (!Utils.checkPermission(requireContext()))
        {
            val dialogInstance = AlertDialog.Builder(requireContext()).create()
            val dialogView = layoutInflater.inflate(R.layout.require_permission_dialog, null)
            dialogInstance.setView(dialogView)
            dialogView.findViewById<TextView>(R.id.btn_accept).setOnClickListener {
                dialogInstance.dismiss()
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
            dialogInstance.show()
        }

        swipeRefresh.setOnRefreshListener(this)

        btnSpeedUp.setOnClickListener{
            val apps = memUsageAdapter.getData()
            val iterator = apps.value!!.listIterator()
            while(iterator.hasNext()){
                val position = iterator.nextIndex()
                val item = iterator.next()
                MemoryUtils.getInstance(requireContext()).killProcessByPackageName(item.packageName)
                //iterator.remove()
                //memUsageAdapter.notifyItemRemoved(position)
                //memUsageAdapter.notifyItemRangeChanged(position, memUsageAdapter.itemCount)
            }
            //memUsageAdapter.getData().value = memUsageAdapter.getData().value
        }
    }

    override fun onRefresh() {
        swipeRefresh.isRefreshing = true
/*        if (isHigherAverageMem)*/
            runningAppsViewModel.retrieveRunningApps()
        /*else
            swipeRefresh.isRefreshing = false*/
    }
}