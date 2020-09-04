package com.example.fillrammemory.controllers

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.fillrammemory.classes.Memory
import com.example.fillrammemory.R
import com.example.fillrammemory.services.MemoryService
import com.example.fillrammemory.utils.Constants
import com.example.fillrammemory.utils.MemoryUtils
import com.example.fillrammemory.viewModels.MemoryInfoViewModel
import kotlinx.android.synthetic.main.fragment_app_info.*

class AppInfoFragment : Fragment(), View.OnClickListener {

    private val viewModel: MemoryInfoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        btnFree.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.btnFree) {
            val intent = Intent(requireContext(), MemoryService::class.java)
            intent.putExtra(Constants.WORK_TYPE, Constants.FREE_MEM_JOB)
            MemoryService.startServiceExecute(requireContext(), intent)
        }
    }
}