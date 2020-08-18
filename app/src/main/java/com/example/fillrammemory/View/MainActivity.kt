package com.example.fillrammemory.View
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.fillrammemory.R
import com.example.fillrammemory.Utils.MemoryUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), Runnable{

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var memoryUtils: MemoryUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        memoryUtils = MemoryUtils.getInstance(this)
    }

    override fun onResume() {
        super.onResume()
        handler.post(this)
    }

    override fun run() {
        memoryUtils.updateMemInfo()
        totalValue.text = MemoryUtils.formatToString(memoryUtils.getTotalRam().toDouble())
        freeValue.text = MemoryUtils.formatToString(memoryUtils.getAvailableRam().toDouble())
        usedValue.text = MemoryUtils.formatToString((memoryUtils.getTotalRam().minus(memoryUtils.getAvailableRam())).toDouble())
        progressBar.progress = memoryUtils.getAvailableMemInPercentage()
        progressPercentage.text = "${memoryUtils.getAvailableMemInPercentage()}%"
        handler.postDelayed(this, 500)
    }
}