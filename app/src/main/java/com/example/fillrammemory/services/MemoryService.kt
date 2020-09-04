package com.example.fillrammemory.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.content.ContextCompat.startForegroundService
import com.example.fillrammemory.R
import com.example.fillrammemory.controllers.MainActivity
import com.example.fillrammemory.utils.Constants
import com.example.fillrammemory.utils.MemoryUtils
import java.nio.ByteBuffer
import kotlin.system.exitProcess

class MemoryService : Service() {

    init {
        try{
            System.loadLibrary("nativeLib")
        } catch(e: UnsatisfiedLinkError){
            e.printStackTrace()
            exitProcess(0)
        }
    }

    inner class ServiceBinder : Binder() {
        val serviceInstance: MemoryService
        get() = this@MemoryService
    }

    private val mHandleThread: HandlerThread = HandlerThread("Foreground Service HandlerThread")
    private var mServiceHandler: Handler? = null
    private val mBinder: IBinder = ServiceBinder()
    private var notificationBuilder: Notification.Builder? = null
    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        mHandleThread.start()
        mServiceHandler = Handler(mHandleThread.looper)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = this.getNotificationBuilder()
        startForeground(ID, notificationBuilder?.build())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        /*Log.d(TAG, "onStartCommand executed with startId: $startId")
        val workType = intent.getStringExtra(Constants.WORK_TYPE)
        if (workType != null) {
            if (workType == Constants.GEN_VAR_JOB) {
                Log.d(TAG, "onStartCommand executed with GEN_VAR_JOB")
                val value = intent.getIntExtra(Constants.MSG_VALUE, 0)
                val unit = intent.getStringExtra(Constants.MSG_UNIT)
                val bytesValue =  MemoryUtils.getInstance(this).convertValueToBytes(value.toLong(), unit ?: "MB");
                mServiceHandler?.post {
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
                mServiceHandler?.post {

                    val iterator = mAllocations.iterator()

                    while (iterator.hasNext()){
                        freeVar(iterator.next())
                        iterator.remove()
                    }
                    mAllocationSize = 0
                    val broadcastIntent = Intent()
                    broadcastIntent.action = Constants.CREATED_VAR
                    broadcastIntent.putExtra(Constants.DATA, mAllocationSize)
                    sendBroadcast(broadcastIntent)
                }
            }
        }
        else {
            Log.d(TAG,"with a null intent. It has been probably restarted by the system." )
        }*/
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandleThread.quitSafely()
        notificationManager?.cancelAll()
    }

    private fun getNotificationBuilder(): Notification.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChanel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChanel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent =  PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val builder: Notification.Builder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(this, CHANNEL_ID) else Notification.Builder(this)
        return builder
            .setContentTitle(getString(R.string.app_name))
            .setContentText(String.format(getString(R.string.str_noti_desc), mAllocationSize))
            .setContentIntent(pendingIntent)
    }

    fun allocateVariable(value: Long, unit: String) {
        val bytesValue =  MemoryUtils.getInstance(this).convertValueToBytes(value, unit);
       mServiceHandler?.post {
            val arr = varGenerator(bytesValue)
            if (arr != null) {
                mAllocations.add(arr)
                mAllocationSize += arr.capacity()
                val broadcastIntent = Intent()
                broadcastIntent.action = Constants.CREATED_VAR
                broadcastIntent.putExtra(Constants.DATA, mAllocationSize)
                sendBroadcast(broadcastIntent)
                Log.d(TAG, broadcastIntent.toString())
            }
        }
    }

    fun freeAllocatedVariable() {
        mServiceHandler?.post {
            val iterator = mAllocations.iterator()
            while (iterator.hasNext()){
                freeVar(iterator.next())
                iterator.remove()
            }
            mAllocationSize = 0
            val broadcastIntent = Intent()
            broadcastIntent.action = Constants.CREATED_VAR
            broadcastIntent.putExtra(Constants.DATA, mAllocationSize)
            sendBroadcast(broadcastIntent)
        }
    }

    private fun updateNotification() {
        notificationManager?.notify(ID, notificationBuilder?.setContentText(MemoryUtils.formatToString(mAllocationSize))?.build())
    }
    companion object {
        var TAG = MemoryService::class.simpleName ?: "MEMORY SERVICE"
        private const val ID = 1
        private const val CHANNEL_ID = "Service Notification"
        private var mAllocations: ArrayList<ByteBuffer> = ArrayList()
        private var mAllocationSize: Long = 0

        fun startServiceExecute(context: Context, intent: Intent) = startForegroundService(context, intent)

        fun stopService(context: Context) {
            val stopIntent = Intent(context, MemoryService::class.java)
            context.stopService(stopIntent)
        }
    }
    private external fun varGenerator(size: Long): ByteBuffer?
    private external fun freeVar(buff: ByteBuffer)
}