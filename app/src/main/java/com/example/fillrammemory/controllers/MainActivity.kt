package com.example.fillrammemory.controllers
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.fillrammemory.R
import com.example.fillrammemory.utils.GetMemoryThread
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(){

    private lateinit var getMemoryThread: GetMemoryThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navController = findNavController(R.id.nav_host_fragment)
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

