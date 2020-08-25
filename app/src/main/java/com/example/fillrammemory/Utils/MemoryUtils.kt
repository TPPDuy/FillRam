package com.example.fillrammemory.Utils

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.example.fillrammemory.Services.MemoryService
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.Exception
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class MemoryUtils(context: Context) {

    /*
     Đơn vị chuẩn để so sánh và tính toán là KB
    */
    private var activityManager: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val memoryInfo = ActivityManager.MemoryInfo()

    init{
        updateMemInfo()
    }

    fun  updateMemInfo(){
        activityManager.getMemoryInfo(memoryInfo)
    }

    fun getTotalRam(): Long{
        return memoryInfo.totalMem
    }

    fun getAvailableRam(): Long{
        return memoryInfo.availMem
    }

    fun isLowMem(): Boolean{
        return memoryInfo.lowMemory
    }

    fun getMemThreshold(): Long{
        return memoryInfo.threshold
    }

    fun getAvailableMemInPercentage(): Int{
        return ((memoryInfo.availMem.toDouble() / memoryInfo.totalMem) * 100).toInt()
    }

    private fun convertMBToBytes(mbValue: Int): Int {
        return mbValue * MBToB;
    }
    private fun convertGBToBytes(mbValue: Int): Int {
        return mbValue * GBToB;
    }

    private fun handleValueInput(strValue: String) : Int {
        val match = Regex("(\\d+) (\\w+)").find(strValue)!!
        val (value, unit) = match.destructured
        //Log.d(MemoryService.TAG, "$value has ${unit} unit")
        var convertValue: Int = 0
        when (unit) {
            "KB" -> {}
            "MB" -> {
                convertValue = convertMBToBytes(value.toInt())
            }
            "GB" -> {
                convertValue = convertGBToBytes(value.toInt())
            }
        }
        return convertValue;
    }

    fun increaseMemory(strValue: String){
        val value = handleValueInput(strValue);
         Log.d(MemoryService.TAG + "Total Memory:  ", formatToString(memoryInfo.totalMem.toDouble()))
        try{
            val byte: ByteArray = ByteArray(value)
            v.add(byte)
            Log.d(MemoryService.TAG, "$value has ${byte.size} size")

        } catch(e: Exception){
            e.printStackTrace()
        }

        Log.d(MemoryService.TAG, "The numbers of size elements: ${v.size} ")
        Log.d(MemoryService.TAG + "Free Memory:  ", formatToString(memoryInfo.availMem.toDouble()))

    }

    companion object{
        private const val MBToKB = 1024.0
        private const val GBToKB = 1024.0 * 1024.0
        private const val MBToB = 1024 * 1024
        private const val GBToB = 1024 * 1024 * 1024
        private var v: ArrayList<Any> = ArrayList()

        private var instance: MemoryUtils? = null
        fun getInstance(context: Context): MemoryUtils{
            if (instance == null)
                instance = MemoryUtils(context)
            return instance as MemoryUtils
        }

        fun formatToString(value: Double): String {
            val twoDecimalFormat: DecimalFormat = DecimalFormat("#.##")
            val mbValue = value.div(1024).div(MBToKB)
            val gbValue = value.div(1024).div(GBToKB)
            val result : String
            result = if (gbValue > 1) {
                twoDecimalFormat.format(gbValue).plus(" GB")
            } else if (mbValue > 1) {
                twoDecimalFormat.format(mbValue).plus(" MB")
            } else {
                twoDecimalFormat.format(value).plus(" KB")
            }
            return result
        }

        /*
        * type:
        * 1 - Total
        * 2 - Free
        * */
        fun readRamFromSystem(type: Int): Double {
            val reader: RandomAccessFile
            try {
                reader = RandomAccessFile("/proc/meminfo", "r")
                var resultMem = 0.0
                var loadLine = ""
                loadLine = reader.readLine()
                if (type == 2) loadLine = reader.readLine()

                val p = Pattern.compile("(\\d+)")
                val m = p.matcher(loadLine)
                var value: String? = null
                while (m.find()) {
                    value = m.group(1)
                }
                reader.close()
                resultMem = value?.toDouble() ?: 0.0

                return resultMem

            } catch(exception:IOException){
                exception.printStackTrace()
                return 0.0
            }
        }
    }
}