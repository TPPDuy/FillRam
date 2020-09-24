package com.example.fillrammemory.controllers

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.example.fillrammemory.R

class ProgressDialog(context: Context) : Dialog(context, R.style.CoreTheme_Dialog) {
    private var mOwnerActivity: Activity? = null

    init {
        mOwnerActivity = if (context is Activity) context else null
        if (mOwnerActivity != null) setOwnerActivity(mOwnerActivity!!)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mOwnerActivity = ownerActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.progress_dialog)
        val window = window
        if (window != null) {
            val params = window.attributes
            params.windowAnimations = R.style.CoreTheme_AnimDialog_Fade
            window.attributes = params
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}