package com.example.fillrammemory.Services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.fillrammemory.controllers.MainActivity
import com.example.fillrammemory.controllers.SystemInfoFragment
import com.example.fillrammemory.utils.Constants
import com.example.fillrammemory.utils.MemoryUtils
import java.nio.ByteBuffer

class MemoryForegroundService : Service() {

    private lateinit var mHandleThread: HandlerThread
    private lateinit var mServiceHAandler: Handler

    override fun onCreate() {
        super.onCreate()
        var notification = createNotification()
        startForeground(1, notification)

        Log.d(TAG, "The Foreground service has been created..")

        mHandleThread = HandlerThread("Foreground Service HandlerThread")
        mHandleThread.start()

        mServiceHAandler = Handler(mHandleThread.looper)

    }



    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand executed with startId: $startId")
        val workType = intent.getStringExtra(Constants.WORK_TYPE)
        if (workType != null) {
            if (workType == Constants.GEN_VAR_JOB) {
                Log.d(TAG, "onStartCommand executed with GEN_VAR_JOB")
                val value = intent.getIntExtra(Constants.MSG_VALUE, 0)
                val unit = intent.getStringExtra(Constants.MSG_UNIT)
                val bytesValue =  MemoryUtils.getInstance(this).convertValueToBytes(value, unit ?: "MB");

                mServiceHAandler.post {
                    val arr = varGenerator(bytesValue)
                    if (arr != null) {
                        mAllocations.add(arr)
                        mAllocationSize += arr.capacity()
                        Log.d("ARR SIZE", arr.capacity().toString())
                        Log.d("ARR SIZES", mAllocations.size.toString())
                        Log.d("SIZE", mAllocationSize.toString())
                        val broadcastIntent = Intent()
                        broadcastIntent.action = Constants.CREATED_VAR
                        broadcastIntent.putExtra(Constants.DATA, mAllocationSize)
                        sendBroadcast(broadcastIntent)
                        Log.d(TAG, broadcastIntent.toString())
                    }
                }
            } else if (workType == Constants.FREE_MEM_JOB) {
                Log.d(TAG, "onStartCommand executed with REE_MEM_JOB")
                mServiceHAandler.post {
                    for (buff in MemoryForegroundService.mAllocations) {
                        freeVar(buff)
                        MemoryForegroundService.mAllocationSize = 0
                        val broadcastIntent = Intent()
                        broadcastIntent.action = Constants.CREATED_VAR
                        broadcastIntent.putExtra(Constants.DATA, MemoryForegroundService.mAllocationSize)
                        sendBroadcast(broadcastIntent)
                    }
                }
               //stopSelf(startId)
            }
        }
        else {
            Log.d(TAG,"with a null intent. It has been probably restarted by the system." )
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "MEMORY SERVICE"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChanel = NotificationChannel(
                notificationChannelId,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager!!.createNotificationChannel(serviceChanel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =  PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val builder: Notification.Builder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        )else Notification.Builder(this)
        return builder
            .setContentTitle("Foreground service memory")
            .setContentText("This is running service...")
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_HIGH)
            .build()
    }

    companion object {
        var TAG = MemoryForegroundService::class.simpleName ?: "MEMORY FOREGROUND SERVICE"
        private var mAllocations: ArrayList<ByteBuffer> = ArrayList()
        private var mAllocationSize: Long = 0
        init {
            try{
                System.loadLibrary("nativeLib")
            } catch(e: UnsatisfiedLinkError){
                e.printStackTrace()
            }
        }

        fun startServiceExecute(context: Context, intent: Intent) {
            ContextCompat.startForegroundService(context, intent)
        }
        fun stopService(context: Context) {
            val stopIntent = Intent(context, MemoryForegroundService::class.java)
            context.stopService(stopIntent)
        }
    }
    private external fun varGenerator(size: Long): ByteBuffer?
    private external fun freeVar(buff: ByteBuffer)
}