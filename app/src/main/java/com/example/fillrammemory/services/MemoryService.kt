package com.example.fillrammemory.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.example.fillrammemory.R
import com.example.fillrammemory.classes.VarHolder
import com.example.fillrammemory.controllers.MainActivity
import com.example.fillrammemory.utils.Constants
import com.example.fillrammemory.utils.MemoryUtils
import java.lang.Exception
import java.nio.ByteBuffer
import kotlin.system.exitProcess


class MemoryService : Service() {

    init {
        try{
            System.loadLibrary("nativeLib")
        } catch (e: UnsatisfiedLinkError){
            e.printStackTrace()
            exitProcess(0)
        }
    }

    inner class ServiceBinder : Binder() {
        val serviceInstance: MemoryService
        get() = this@MemoryService
    }


    private var mBinder: IBinder? = null
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        mBinder = ServiceBinder()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = getNotificationBuilder()
        startForeground(ID, notificationBuilder!!.build())
        isRunning = true
        Log.d("Service", "Service created")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("Service", "Service bind")
        return mBinder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("Service", "Service start command")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        //mHandleThread?.quitSafely()
        notificationManager?.cancelAll()
        isRunning = false
        Log.d("Service", "Destroy")
    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChanel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChanel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)

        val notificationLayout = RemoteViews(packageName, R.layout.notification_custom)

        notificationLayout.setOnClickPendingIntent(R.id.btnFree, onButtonNotificationClicked(R.id.btnFree))
        notificationLayout.setOnClickPendingIntent(R.id.btnStop, onButtonNotificationClicked(R.id.btnStop))

        val pendingIntent =  PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val builder: NotificationCompat.Builder = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ) else NotificationCompat.Builder(this)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setContent(notificationLayout)
        return builder
    }

    private fun onButtonNotificationClicked(@IdRes id: Int): PendingIntent? {
        val intent = Intent(Constants.NOTIFICATION_BUTTON_CLICKED)
        intent.putExtra(Constants.BUTTON_CLICKED, id)
        return PendingIntent.getBroadcast(this, id, intent, 0)
    }

    fun allocateVariable(value: Long, unit: String) {

        val intent = Intent(Constants.UPDATE_STATE)
        intent.putExtra(Constants.DATA, true)
        sendBroadcast(intent)

        val bytesValue =  MemoryUtils.getInstance(this).convertValueToBytes(value, unit)
        mServiceHandler?.post {
            val arr = varGenerator(bytesValue)
            if (arr != null) {
                mAllocations.addVar(arr)
                mAllocationSize += arr.capacity()
            }
            intent.removeExtra(Constants.DATA)
            intent.putExtra(Constants.DATA, false)
            sendBroadcast(intent)
        }
    }

    fun freeAllocatedVariable() {
        val intent = Intent(Constants.UPDATE_STATE)
        intent.putExtra(Constants.DATA, true)
        try{
            sendBroadcast(intent)
        }catch (ex: Exception){ }

        mServiceHandler?.post {
            val iterator = mAllocations.getIterator() as MutableIterator
            var mTemp: ByteBuffer
            while (iterator.hasNext()) {
                mTemp = iterator.next()
                mAllocationSize -= mTemp.capacity()
                freeVar(mTemp)
                iterator.remove()
                Thread.sleep(200)
            }
            intent.removeExtra(Constants.DATA)
            intent.putExtra(Constants.DATA, false)
            try{
                sendBroadcast(intent)
            }catch (ex:Exception){}
        }


    }

    companion object {
        var TAG = MemoryService::class.simpleName ?: "MEMORY SERVICE"
        private const val ID = 1
        private const val CHANNEL_ID = "Service Notification"
        private var mAllocations: VarHolder<ByteBuffer> = VarHolder()
        private val mInstance = MemoryService()
        var mAllocationSize: Long = 0
        var isRunning: Boolean = false

        private var mHandleThread: HandlerThread? = null
        private var mServiceHandler: Handler? = null

        fun getInstance(): MemoryService{
            return mInstance
        }
        fun startServiceExecute(context: Context, intent: Intent) {
            mHandleThread = HandlerThread("Foreground Service HandlerThread")
            mHandleThread!!.start()
            mServiceHandler = Handler(mHandleThread!!.looper)
            startForegroundService(
                context,
                intent
            )
        }

        fun stopService(context: Context?) {
            val stopIntent = Intent(context, MemoryService::class.java)
            context?.stopService(stopIntent)
            mHandleThread?.quitSafely()
        }
    }
    private external fun varGenerator(size: Long): ByteBuffer?
    private external fun freeVar(buff: ByteBuffer)
}