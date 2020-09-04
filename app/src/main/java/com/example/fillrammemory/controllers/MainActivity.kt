package com.example.fillrammemory.controllers
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.fillrammemory.R
import com.example.fillrammemory.services.MemoryService
import com.example.fillrammemory.services.MemoryServiceConnection
import com.example.fillrammemory.utils.GetMemoryThread
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(){

    private lateinit var getMemoryThread: GetMemoryThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration.Builder(R.id.systemInfoFragment, R.id.appInfoFragment).build()
        setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupWithNavController(bottom_nav, navController)
    }

    override fun onStart() {
        super.onStart()
        getMemoryThread = GetMemoryThread("THREAD", this)
        getMemoryThread.start()
    }

    override fun onStop() {
        super.onStop()
        getMemoryThread.quitSafely()
    }
}

