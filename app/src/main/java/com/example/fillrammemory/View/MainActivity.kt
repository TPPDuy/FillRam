package com.example.fillrammemory.View

import android.app.ActivityManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fillrammemory.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val activityManager= applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        var memoryInfo = ActivityManager.MemoryInfo();

        activityManager.getMemoryInfo(memoryInfo);


    }
}