package com.example.fillrammemory.Controller
import android.os.*
import android.content.Intent
import android.text.format.Formatter
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fillrammemory.R
import com.example.fillrammemory.Services.MemoryService
import android.util.Log
import android.widget.TextView
import com.example.fillrammemory.Utils.Constants
import com.example.fillrammemory.Utils.GetMemoryThread
import com.example.fillrammemory.Utils.MemoryRunnable
import com.example.fillrammemory.model.Memory
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity(), View.OnClickListener{

    private lateinit var memoryRunnable: MemoryRunnable
    private lateinit var getMemoryThread: GetMemoryThread
    private lateinit var uiHandler: UiHandler

    private lateinit var textTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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

        uiHandler = UiHandler()
        uiHandler.setActivityRef(this)
    }

    override fun onStart() {
        super.onStart()

        getMemoryThread = GetMemoryThread("GetMemoryThread", uiHandler)
        getMemoryThread.start()

        memoryRunnable = MemoryRunnable(getMemoryThread, this)
        memoryRunnable.start()

    }

    override fun onResume() {
        super.onResume()
    }

    private fun handleIncreaseMem(value: Int, unit: String) {
        val intent = Intent(this, MemoryService::class.java)
        intent.putExtra(Constants.MSG_VALUE, value)
        intent.putExtra(Constants.MSG_UNIT, unit)
        MemoryService.enqueueWork(this, intent)
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn100 -> {
                handleIncreaseMem(100,"MB" )
            }
            R.id.btn200 -> {
                handleIncreaseMem(200, "MB")
            }
            R.id.btn400 -> {
                handleIncreaseMem(400, "MB")
            }
            R.id.btn500 -> {
                handleIncreaseMem(500, "MB")
            }
            R.id.btn700 -> {
                handleIncreaseMem(700, "MB")
            }
            R.id.btn1 -> {
                handleIncreaseMem(1,"GB")
            }
        }
    }
    class UiHandler : Handler() {
        private lateinit var mActivityRef: WeakReference<MainActivity>

        fun setActivityRef(mainActivity: MainActivity) {
            mActivityRef = WeakReference(mainActivity)
        }


        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val activity = mActivityRef.get()
            if (activity == null || activity.isFinishing || activity.isDestroyed) {
                removeCallbacksAndMessages(null)
                return;
            }
            Log.d("THREAD ",  msg.toString())

            val memoryInfo: Memory = msg.obj as Memory;
            activity.totalValue.text = Formatter.formatFileSize(activity, memoryInfo.total)
            activity.freeValue.text = Formatter.formatFileSize(activity, memoryInfo.available)
            activity.usedValue.text = Formatter.formatFileSize(activity, (memoryInfo.total.minus(memoryInfo.available)))
            activity.progressBar.progress = memoryInfo.availablePercent
            activity.progressPercentage.text = "${ memoryInfo.availablePercent}%"
        }

    }

}

