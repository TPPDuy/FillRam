package com.example.fillrammemory.View

import android.os.*
import androidx.appcompat.app.AppCompatActivity
import com.example.fillrammemory.R
import com.example.fillrammemory.Utils.Constants
import com.example.fillrammemory.Utils.GetMemoryThread
import com.example.fillrammemory.Utils.MemoryUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), Runnable, Handler.Callback{
    val getInfoThread = GetMemoryThread("MEMORY_INFO_THREAD")
    private val handler = Handler(Looper.getMainLooper(), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun run() {
        /*
        val totalMemInfo = MemoryUtils.readRamFromSystem(1)
        val freeMemInfo = MemoryUtils.readRamFromSystem(2)

        val msg1 = Message()
        val msg2 = Message()
        msg1.what = Constants.MSG_UPDATE_TOTAL_MEM
        msg1.obj = totalMemInfo
        msg2.what = Constants.MSG_UPDATE_FREE_MEM
        msg2.obj = freeMemInfo

        handler.sendMessage(msg1)
        handler.sendMessage(msg2)
        */
    }

    override fun handleMessage(msg: Message): Boolean {
        when(msg.what){
            Constants.MSG_UPDATE_TOTAL_MEM -> {
                totalValue.text = msg.obj as String
                return true
            }
            Constants.MSG_UPDATE_FREE_MEM -> {
                freeValue.text = msg.obj as String
                return true
            }
            Constants.MSG_UPDATE_USED_MEM -> {
                usedValue.text = msg.obj as String
                return true
            }
        }
        return false
    }
}