package com.example.fillrammemory.controllers

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
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.fillrammemory.R
import com.example.fillrammemory.adapters.MemUsageAppAdapter
import com.example.fillrammemory.callback.CoreCallback
import com.example.fillrammemory.classes.AppInfo
import com.example.fillrammemory.classes.Memory
import com.example.fillrammemory.utils.MemoryUtils
import com.example.fillrammemory.utils.Utils
import com.example.fillrammemory.viewModels.MemoryInfoViewModel
import com.example.fillrammemory.viewModels.RunningAppsViewModel
import kotlinx.android.synthetic.main.fragment_memory_info.*

class MemoryInfoFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, CoreCallback.WithPare<Int, AppInfo> {

    private lateinit var memUsageAdapter: MemUsageAppAdapter
    private val viewModel: MemoryInfoViewModel by activityViewModels()
    private val runningAppsViewModel: RunningAppsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_memory_info, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memUsageAdapter = MemUsageAppAdapter(requireContext(), runningAppsViewModel.getRunningApps().value, this)

        listUsageApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = memUsageAdapter
        }

        viewModel.getMemoryInfo().observe(viewLifecycleOwner, Observer<Memory>{ mem ->
            run {
                val percentage = ((mem.total.minus(mem.available).times(100)).div(mem.total))
                percentageText.text = percentage.toString()
                when {
                    percentage >= 75 -> {
                        activity?.window?.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.colorOrangeLight)
                        percentageHolder.background = getDrawable(requireContext(), R.drawable.gradient_orange_background)
                    }
                    percentage >= 65 -> {
                        activity?.window?.statusBarColor = ContextCompat.getColor(requireActivity(), R.color.colorAccent)
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
                memUsageAdapter.clearData()
                memUsageAdapter.addListData(listApps)
            }
        })

        memUsageAdapter.getData().observe(viewLifecycleOwner, Observer { data ->
            run {
                swipeRefresh.isRefreshing = false
                if (data.size == 0) {
                    emptyItemHolder?.visibility = View.VISIBLE
                    totalApps?.visibility = View.GONE
                    checkboxAll?.visibility = View.GONE
                    btnSpeedUp.isEnabled = false
                }
                else {
                    emptyItemHolder?.visibility = View.GONE
                    totalApps?.visibility = View.VISIBLE
                    checkboxAll?.visibility = View.VISIBLE
                    btnSpeedUp.isEnabled = true
                }
            }
        })

        memUsageAdapter.getCheckedCount().observe(viewLifecycleOwner, Observer { count ->
            run {
                totalApps?.text = String.format("%d %s", count, getString(R.string.str_apps))
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (!Utils.checkPermission(requireContext()))
        {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.require_permission_dialog, null)
            dialogBuilder.setView(dialogView)
            dialogView.findViewById<TextView>(R.id.btn_accept).setOnClickListener { startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            ) }
            dialogBuilder.show()
        }

        swipeRefresh.setOnRefreshListener(this)

        btnSpeedUp.setOnClickListener{
            val apps = memUsageAdapter.getData()
            val iterator = apps.value!!.listIterator()
            while(iterator.hasNext()){
                val position = iterator.nextIndex()
                val item = iterator.next()
                if (item.isSelected){
                    MemoryUtils.getInstance(requireContext()).killProcessByPackageName(item.packageName)
                    iterator.remove()
                    memUsageAdapter.notifyItemRemoved(position)
                    memUsageAdapter.notifyItemRangeChanged(position, memUsageAdapter.itemCount)
                }
            }
            memUsageAdapter.getData().value = memUsageAdapter.getData().value
        }
        checkboxAll.setOnClickListener {
            memUsageAdapter.changeSelectedStateAll(checkboxAll.isChecked)
        }

    }

    override fun onRefresh() {
        swipeRefresh.isRefreshing = true
        runningAppsViewModel.retrieveRunningApps()
    }

    override fun run(p1: Int, p2: AppInfo) {
        p2.isSelected = !p2.isSelected
        checkboxAll.isChecked = false
        memUsageAdapter.getData().value = memUsageAdapter.getData().value
        memUsageAdapter.notifyItemChanged(p1)
    }
}