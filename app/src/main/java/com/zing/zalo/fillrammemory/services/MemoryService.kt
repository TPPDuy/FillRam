package com.zing.zalo.fillrammemory.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startForegroundService
import com.zing.zalo.fillrammemory.R
import com.zing.zalo.fillrammemory.broadcast.NotificationBroadcast
import com.zing.zalo.fillrammemory.classes.VarHolder
import com.zing.zalo.fillrammemory.controllers.MainActivity
import com.zing.zalo.fillrammemory.utils.Constants
import com.zing.zalo.fillrammemory.utils.MemoryUtils
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

        mServiceHandler?.post {
            allocate(value, unit)
            intent.removeExtra(Constants.DATA)
            intent.putExtra(Constants.DATA, false)
            sendBroadcast(intent)
        }
    }

    fun handleFreeAllAllocated() {
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

    fun handleFreeCustomAllocated(value: Long){
        if (!getAllocateState()){
            val intent = Intent(Constants.UPDATE_STATE)
            intent.putExtra(Constants.DATA, true)
            try{
                sendBroadcast(intent)
            }catch (ex: Exception){
                ex.printStackTrace()
            }

            mServiceHandler?.post {
                var suitableSpace: ByteBuffer? = null
                var mTemp: ByteBuffer
                val iterator = mAllocations.getIterator()
                while(iterator.hasNext()){
                    mTemp = iterator.next()
                    if (mTemp.capacity() >= value) {
                        Log.e(TAG, "found space: ${mTemp.capacity()}")
                        suitableSpace = mTemp
                        break
                    }
                }
                if (suitableSpace != null){
                    val result = changeAllocatedSize(suitableSpace, suitableSpace.capacity() - value)
                    mAllocationSize -= value
                    if (result!=null){
                        Log.e(TAG, "realloc success: ${result.capacity()}")
                        mAllocations.removeElement(suitableSpace)
                        mAllocations.addVar(result)
                    } else {
                        mAllocations.removeElement(suitableSpace)
                        allocate(value, "KB")
                    }
                }else{
                    //can not find a ByteBuffer that has size larger than free value
                    //We will remove from the last and up to the head
                    var freeValue = value
                    var lastElement = mAllocations.getLastElement()
                    while(lastElement?.capacity() ?: 0 < value){
                        freeValue -= lastElement?.capacity() ?: 0
                        mAllocationSize -= lastElement?.capacity() ?: 0
                        freeVar(mAllocations.removeLastElement())
                        lastElement = mAllocations.getLastElement()
                    }

                    if(freeValue > 0){
                        val newSize = if(lastElement != null) (lastElement.capacity() - value) else 0
                        val result = changeAllocatedSize(lastElement, newSize)
                        mAllocationSize -= value
                        if (result!=null){
                            Log.e(TAG, "realloc success: ${result.capacity()}")
                            mAllocations.removeElement(lastElement)
                            mAllocations.addVar(result)
                        } else {
                            mAllocations.removeElement(lastElement)
                            allocate(value, "KB")
                        }
                    }
                }
                Thread.sleep(2000)
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
        val bytesValue =  MemoryUtils.convertValueToBytes(value, unit)
        Log.d("Byte value", bytesValue.toString())
        if(bytesValue >= Integer.MAX_VALUE){
            handleDivideAllocation(value, unit)
        } else {
            val arr = varGenerator(bytesValue)
            if (arr != null) {
                Log.d("Allocate new ", "success")
                mAllocations.addVar(arr)
                mAllocationSize += arr.capacity()
            } else {
                //Can not find enough sequence space for allocate this value -> divide into smaller
                handleDivideAllocation(value, unit)
            }
        }
    }

    private fun extendAllocatedSpace(value: Long, unit: String){
        var bytesValue =  MemoryUtils.convertValueToBytes(value, unit)
        val lastElementSize = mAllocations.getLastElement()?.capacity() ?: 0
        var newExtendSpace: ByteBuffer? = null
        if (lastElementSize < Int.MAX_VALUE){
            val additionalSize = if(lastElementSize + bytesValue < Int.MAX_VALUE) bytesValue else (Int.MAX_VALUE - lastElementSize).toLong()
            newExtendSpace = varExtend(mAllocations.removeLastElement(), (additionalSize + lastElementSize).toLong())
            if (newExtendSpace!=null) {
                mAllocations.addVar(newExtendSpace)
                //can not extend existed space and get the old buff -> allocate new space
                if(newExtendSpace.capacity() == lastElementSize){
                    allocateNewSpace(value, unit)
                }
                //extend space successfully
                else{
                    if(mAllocations.getLength() == 1)
                        mAllocationSize = newExtendSpace.capacity().toLong()
                    else {
                        mAllocationSize -= lastElementSize.toLong()
                        mAllocationSize += newExtendSpace.capacity()
                    }
                }
            } else {
                Log.e("ALLOCATION", "Failed to allocate")
                //Can not find enough sequence space for allocate this value -> divide into smaller
                handleDivideAllocation(value, unit)
            }
            bytesValue -= additionalSize
            if(bytesValue>1024)
                allocateNewSpace(bytesValue/1024, "KB")
        } else{
            allocateNewSpace(value, unit);
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
        //change to KB value
        val valueInKB = MemoryUtils.convertValueToBytes(value, unit)/1024;
        allocate(valueInKB/2, "KB")
        allocate(valueInKB/2, "KB")
    }
    companion object {
        var TAG = MemoryService::class.simpleName ?: "MEMORY SERVICE"
        private const val ID = 1
        private const val CHANNEL_ID = "Service Notification"
        private var mAllocations: VarHolder<ByteBuffer> = VarHolder()
        var mAllocationSize: Long = /*Debug.getNativeHeapAllocatedSize()*/ 0
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
    private external fun varExtend(buff: ByteBuffer?, newSize: Long): ByteBuffer?
    private external fun freeVar(buff: ByteBuffer?)
    private external fun changeAllocatedSize(buff: ByteBuffer?, newSize: Long): ByteBuffer?
}