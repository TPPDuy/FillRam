package com.zing.zalo.fillrammemory.controllers
import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.zing.zalo.fillrammemory.R
import kotlinx.android.synthetic.main.custom_size_dialog.*

class CustomSizeDialog(val type: String) : DialogFragment(), View.OnClickListener, AdapterView.OnItemSelectedListener{

    interface DialogListener {
        fun onFinishDialog(value: Long, unit: String, type: String)
    }

    private lateinit var listener: DialogListener
    private lateinit var btnCancel: Button
    private lateinit var btnOK: Button
    private lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.custom_size_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)

        val fmg: Fragment? = targetFragment
        if(fmg is DialogListener) {
            listener = fmg
        }

        btnCancel.setOnClickListener(this)
        btnOK.setOnClickListener(this)
        spinner.onItemSelectedListener = this
    }

    private fun setupView(view: View) {
        btnCancel = view.findViewById(R.id.btnCancel)
        btnOK = view.findViewById(R.id.btnOk)
        spinner = view.findViewById(R.id.spinnerUnits)
        edtValue.keyListener = DigitsKeyListener.getInstance("1234567890")

        val adapter = ArrayAdapter<String>(requireContext(),
            android.R.layout.simple_spinner_item, listOfUnits)
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter= adapter


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
                R.id.btnCancel -> {
                    dismiss()
                }
                R.id.btnOk -> {
                    valueResult = edtValue?.text.toString().trim()
                    if(valueResult.isEmpty()) {
                        Toast.makeText(requireContext(),
                            "Value cannot be empty!", Toast.LENGTH_LONG)
                            .show();
                    } else {
                        listener.onFinishDialog(valueResult.toLong(), unitResult, type)
                        dismiss()
                    }
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        unitResult =  spinner.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    companion object {
        const val TAG = "Custom Size Dialog"
        const val TARGET = 300
        private val listOfUnits = arrayOf("KB", "MB", "GB")

        private var valueResult: String = ""
        private var unitResult: String = ""


        fun newInstance(type: String) : CustomSizeDialog{
            return CustomSizeDialog(type);
        }
    }

}