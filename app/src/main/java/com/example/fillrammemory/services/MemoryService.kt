package com.example.fillrammemory.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.example.fillrammemory.R
import com.example.fillrammemory.broadcast.NotificationBroadcast
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
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
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
        val pendingIntent =  PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val freeIntent = Intent(this, NotificationBroadcast::class.java).apply {
            action = Constants.FREE_ACTION
        }
        val stopServiceIntent = Intent(this, NotificationBroadcast::class.java).apply {
            action = Constants.STOP_ACTION
        }
        val freePendingIntent = PendingIntent.getBroadcast(this, 0, freeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val stopServicePendingIntent = PendingIntent.getBroadcast(this, 0, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.str_noti_title))
            .setContentText(getString(R.string.str_noti_content))
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_free_mem_noti, getString(R.string.str_free_allocation), freePendingIntent)
            .addAction(R.drawable.ic_stop, getString(R.string.str_stop_service), stopServicePendingIntent)
        return builder
    }

    fun handleFillRam(value: Long, unit: String) {

        Log.d("ALLOCATION", value.toString() +  unit)
        val intent = Intent(Constants.UPDATE_STATE)
        intent.putExtra(Constants.DATA, true)
        sendBroadcast(intent)

        val bytesValue =  MemoryUtils.getInstance(this).convertValueToBytes(value, unit)
        mServiceHandler?.post {
            allocate(value, unit)
            intent.removeExtra(Constants.DATA)
            intent.putExtra(Constants.DATA, false)
            sendBroadcast(intent)
        }
    }

    fun handleFreeAllocated() {
        if (!getAllocateState()){
            val intent = Intent(Constants.UPDATE_STATE)
            intent.putExtra(Constants.DATA, true)
            try{
                sendBroadcast(intent)
            }catch (ex: Exception){
                ex.printStackTrace()
            }

            mServiceHandler?.post {
                Log.d("DEALLOCATION", "Free")
                val iterator = mAllocations.getIterator() as MutableIterator
                var mTemp: ByteBuffer
                while (iterator.hasNext()) {
                    mTemp = iterator.next()
                    mAllocationSize-=mTemp.capacity()
                    freeVar(mTemp)
                    iterator.remove()
                    Thread.sleep(200)
                }
                intent.removeExtra(Constants.DATA)
                intent.putExtra(Constants.DATA, false)
                try{
                    sendBroadcast(intent)
                }catch (ex:Exception){
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun allocateNewSpace(value: Long, unit: String){
        val bytesValue =  MemoryUtils.getInstance(this).convertValueToBytes(value, unit)
        val arr = varGenerator(bytesValue)
        if (arr != null) {
            mAllocations.addVar(arr)
            mAllocationSize += arr.capacity()
        } else {
            //Can not find enough sequence space for allocate this value -> divide into smaller
            handleDivideAllocation(value, unit)
        }
    }

    private fun extendAllocatedSpace(value: Long, unit: String){
        val bytesValue =  MemoryUtils.getInstance(this).convertValueToBytes(value, unit)
        val oldSize = mAllocations.getLastElement()?.capacity()
        val newExtendSpace = varExtend(mAllocations.removeLastElement(), oldSize ?: 0, bytesValue.toInt())
        if (newExtendSpace!=null) {
            mAllocations.addVar(newExtendSpace)
            //can not extend existed space and get the old buff -> allocate new space
            if(newExtendSpace.capacity() == oldSize){
                allocateNewSpace(value, unit)
            }
            //extend space successfully
            else{
                if(mAllocations.getLength() == 1)
                    mAllocationSize = newExtendSpace.capacity().toLong()
                else {
                    mAllocationSize -= oldSize?.toLong() ?: 0
                    mAllocationSize += newExtendSpace.capacity()
                }
            }
        } else {
            Log.e("ALLOCATION", "Failed to allocate")
            //Can not find enough sequence space for allocate this value -> divide into smaller
            handleDivideAllocation(value, unit)
        }
    }

    private fun allocate(value: Long, unit: String){
        if (mAllocations.getLength() == 0) {
            allocateNewSpace(value, unit)
        } else {
            extendAllocatedSpace(value, unit)
        }
    }
    private fun handleDivideAllocation(value: Long, unit: String){
        if (value % 2 == 0L){
            allocate(value/2, unit)
            allocate(value/2, unit)
        } else {
            var unitNum = when(unit){
                "KB" -> 1
                "MB" -> 2
                "GB" -> 3
                else -> 0
            }
            if(unitNum != 0){
                if (unitNum != 1){
                    //exchange GB/MB to smaller unit
                    val smallerValue = (value.toDouble() * 1024 / 2).toLong()
                    unitNum--
                    val newUnit = when(unitNum){
                        1 -> "KB"
                        2 -> "MB"
                        3 -> "GB"
                        else -> ""
                    }
                    Log.e("DIVIDE ALLOCATION", "Allocate smaller value $smallerValue $newUnit")
                    allocate(smallerValue, newUnit)
                    allocate(smallerValue, newUnit)
                } else {
                    //Its KB value, just divide 2
                    val smallerValue = value/2
                    Log.e("DIVIDE ALLOCATION", "Allocate smaller value $smallerValue $unit")

                    allocate(smallerValue, unit)
                    allocate(smallerValue, unit)
                }

            }

        }
    }
    companion object {
        var TAG = MemoryService::class.simpleName ?: "MEMORY SERVICE"
        private const val ID = 1
        private const val CHANNEL_ID = "Service Notification"
        private var mAllocations: VarHolder<ByteBuffer> = VarHolder()
        var mAllocationSize: Long = Debug.getNativeHeapAllocatedSize()
        private val mInstance = MemoryService()
        var isRunning: Boolean = false

        private var mServiceThread: HandlerThread? = null
        private var mServiceHandler: Handler? = null

        private var isAllocating: Boolean = false

        fun setAllocateState(newState: Boolean){
            synchronized(isAllocating){
                isAllocating = newState
            }
        }

        fun getAllocateState(): Boolean{
            synchronized(isAllocating){
                return isAllocating
            }
        }
        fun getInstance(): MemoryService{
            return mInstance
        }
        fun startServiceExecute(context: Context, intent: Intent) {
            mServiceThread = HandlerThread("Foreground Service HandlerThread")
            mServiceThread!!.start()
            mServiceHandler = Handler(mServiceThread!!.looper)
            startForegroundService(
                context,
                intent
            )
        }
        fun stopService(context: Context?) {
            if(!getAllocateState()){
                val stopIntent = Intent(context, MemoryService::class.java)
                context?.stopService(stopIntent)
                mServiceThread?.quitSafely()
            }
        }
    }
    private external fun varGenerator(size: Long): ByteBuffer?
    private external fun varExtend(buff: ByteBuffer?, oldSize: Int, additionalSize: Int): ByteBuffer?
    private external fun freeVar(buff: ByteBuffer)
}