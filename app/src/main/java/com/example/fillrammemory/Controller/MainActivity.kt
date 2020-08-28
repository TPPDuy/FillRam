package com.example.fillrammemory.Controller
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.fillrammemory.R
import com.example.fillrammemory.Utils.MemoryUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()/*, Runnable, View.OnClickListener*/{

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var memoryUtils: MemoryUtils
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration.Builder(R.id.systemInfoFragment, R.id.appInfoFragment).build()

        setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupWithNavController(bottom_nav, navController)
    }
    /*
    override fun run() {
        memoryUtils.updateMemInfo()
        // totalValue.text = MemoryUtils.formatToString(memoryUtils.getTotalRam().toDouble())
        totalValue.text = Formatter.formatFileSize(this, memoryUtils.getTotalRam())
        freeValue.text = Formatter.formatFileSize(this, memoryUtils.getAvailableRam())
        usedValue.text = Formatter.formatFileSize(this, (memoryUtils.getTotalRam().minus(memoryUtils.getAvailableRam())))
        progressBar.progress = memoryUtils.getAvailableMemInPercentage()
        progressPercentage.text = "${memoryUtils.getAvailableMemInPercentage()}%"
        handler.postDelayed(this, 500)
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
*/
}
