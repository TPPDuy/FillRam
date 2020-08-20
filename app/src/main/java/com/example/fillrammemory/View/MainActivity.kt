package com.example.fillrammemory.View
import android.content.Intent
import android.os.*
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fillrammemory.R
import com.example.fillrammemory.Services.MemoryService
import com.example.fillrammemory.Utils.MemoryUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), Runnable, View.OnClickListener{

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var memoryUtils: MemoryUtils


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        memoryUtils = MemoryUtils.getInstance(this)

        val btn100 = findViewById<Button>(R.id.btn100)
        btn100.setOnClickListener(this)
        val btn200 = findViewById<Button>(R.id.btn200)
        btn200.setOnClickListener(this)
        val btn400 = findViewById<Button>(R.id.btn400)
        btn400.setOnClickListener(this)
        val btn500 = findViewById<Button>(R.id.btn500)
        btn500.setOnClickListener(this)
        val btn700 = findViewById<Button>(R.id.btn700)
        btn700.setOnClickListener(this)
        val btn1 = findViewById<Button>(R.id.btn1)
        btn1.setOnClickListener(this)
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

    fun handleIncreaseMem(value: String) {
        val intent = Intent(this, MemoryService::class.java)
        intent.putExtra("value", value)
        MemoryService.enqueueWork(this, intent)
    }
    override fun onClick(v: View?) {
        val itemId = v?.id
        when (itemId) {
            R.id.btn100 -> {
                handleIncreaseMem("100 MB")
            }
            R.id.btn200 -> {
                handleIncreaseMem("200 MB")
            }
            R.id.btn400 -> {
                handleIncreaseMem("400 MB")
            }
            R.id.btn500 -> {
                handleIncreaseMem("500 MB")
            }
            R.id.btn700 -> {
                handleIncreaseMem("700 MB")
            }
            R.id.btn1 -> {
                handleIncreaseMem("1 GB")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
