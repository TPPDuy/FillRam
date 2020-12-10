package com.zing.zalo.fillrammemory.controllers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zing.zalo.fillrammemory.R
import com.zing.zalo.fillrammemory.adapters.CreatedVarAdapter
import com.zing.zalo.fillrammemory.callback.CoreCallback
import kotlinx.android.synthetic.main.allocated_list_dialog.*

class CreatedVarDialog: DialogFragment(), View.OnClickListener, CoreCallback.With<Int>{
    interface DialogListener {
        fun onFreeAllocatedVar(value: Int)
    }

    private lateinit var listener: DialogListener
    private var createdVarAdapter = CreatedVarAdapter(callback = this)
    private var selectedValue: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.allocated_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()

        val fmg: Fragment? = targetFragment
        if(fmg is DialogListener) {
            listener = fmg
        }
    }

    fun setData(data: ArrayList<Int>){
        createdVarAdapter.setData(data)
    }

    private fun setupView() {
        btnDismiss.setOnClickListener(this)
        btnFree.setOnClickListener(this)
        recyclerView.run {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = createdVarAdapter
        }
        if(createdVarAdapter.itemCount != 0)
            layoutEmpty.visibility = View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }


    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id) {
                R.id.btnDismiss -> dismiss()
                R.id.btnFree -> {
                    listener.onFreeAllocatedVar(selectedValue)
                    dismiss()
                }
            }
        }
    }

    override fun run(t: Int){
        selectedValue = t
    }

    companion object {
        val TAG = CreatedVarDialog::class.java.simpleName
        const val TARGET = 300
    }
}