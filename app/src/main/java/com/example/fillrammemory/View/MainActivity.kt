package com.example.fillrammemory.View
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
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
    fun sendValueToService(value: Int) {
        val intent = Intent(this, MemoryService::class.java)
        intent.putExtra("value", value)
        MemoryService.enqueueWork(this, intent)

    }
    override fun onClick(v: View?) {
        val itemId = v?.id
        when (itemId) {
            R.id.btn100 -> {
                sendValueToService(100)
                Toast.makeText(this, "Btn 100MB Click", Toast.LENGTH_LONG).show()
            }
            R.id.btn200 -> {
                sendValueToService(200)
                Toast.makeText(this, "Btn 200MB Click", Toast.LENGTH_LONG).show()

            }
            R.id.btn400 -> {
                sendValueToService(400)
                Toast.makeText(this, "Btn 400MB Click", Toast.LENGTH_LONG).show()

            }
            R.id.btn500 -> {
                sendValueToService(500)
                Toast.makeText(this, "Btn 500MB Click", Toast.LENGTH_LONG).show()

            }
            R.id.btn700 -> {
                sendValueToService(700)
                Toast.makeText(this, "Btn 700MB Click", Toast.LENGTH_LONG).show()

            }
            R.id.btn1 -> {
                sendValueToService(0)

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}