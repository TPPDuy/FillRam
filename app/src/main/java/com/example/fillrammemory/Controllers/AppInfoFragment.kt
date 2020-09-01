package com.example.fillrammemory.Controllers

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fillrammemory.R
import com.example.fillrammemory.Services.MemoryService
import com.example.fillrammemory.Utils.Constants
import kotlinx.android.synthetic.main.fragment_app_info.*

class AppInfoFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_info, container, false)
    }

    override fun onStart() {
        super.onStart()
        btnFree.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.btnFree) {
            val intent = Intent()
            intent.putExtra(Constants.WORK_TYPE, Constants.FREE_MEM_JOB)
            MemoryService.enqueueWork(requireContext(), intent)
        }
    }


}