package com.example.fillrammemory.utils

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.example.fillrammemory.Services.MemoryForegroundService
import java.io.IOException
import java.io.RandomAccessFile
import java.text.DecimalFormat
import java.util.regex.Pattern

class MemoryUtils(context: Context) {

    /*
     Đơn vị chuẩn để so sánh và tính toán là KB
    */
    private var activityManager: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val memoryInfo = ActivityManager.MemoryInfo()
    private val runtime: Runtime = Runtime.getRuntime()

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

    private fun convertMBToBytes(mbValue: Int): Long {
        return (mbValue * MBToB).toLong();
    }
    private fun convertGBToBytes(mbValue: Int): Long {
        return (mbValue * GBToB).toLong();
    }

    private fun convertKBToBytes(kbValue: Int): Long {
        return (kbValue * KBToB).toLong();
    }

    fun convertValueToBytes(value: Int, unit: String) : Long {
        var convertValue: Long = 0
        when (unit) {
            "KB" -> {
                convertValue = convertKBToBytes(value)
            }
            "MB" -> {
                convertValue = convertMBToBytes(value)
            }
            "GB" -> {
                convertValue = convertGBToBytes(value)
            }
        }
        return convertValue;
    }

    fun getMemoryInfo() {
        val info = " Available Memory =  ${formatToString(memoryInfo.availMem)}\n" +
                "  Total Memory = ${formatToString(memoryInfo.totalMem)}\n" +
                "  Runtime Max Memory =  ${formatToString(runtime.maxMemory())}\n" +
                "  Runtime Total Memory = ${formatToString(runtime.totalMemory())}\n" +
                "  Runtime Free Memory = ${formatToString(runtime.freeMemory())}"
        Log.d(MemoryForegroundService.TAG , info)
    }

    fun isAvailableAdded(value: Int, unit: String) : Boolean {
        var convertValue: Double = 0.00
        when(unit){
            "KB" -> {
                convertValue = value.div(1024).div(GBToKB)
            }
            "MB" -> {
                convertValue = value.div(1024).toDouble()
            }
            "GB" -> {
                convertValue = value.toDouble()
            }
        }
        updateMemInfo()
       Log.d("TAG", convertValue.toString())

        val avaiMem = memoryInfo.availMem.div(1024*1024).div(MBToKB).toDouble()
    Log.d("TAG",avaiMem.toString())

        if(convertValue <= avaiMem) {
            return true
        }
        return false
    }

    companion object{
        private const val MBToKB = 1024.0
        private const val GBToKB = 1024.0 * 1024.0
        private const val MBToB = 1024 * 1024
        private const val GBToB = 1024 * 1024 * 1024
        private const val KBToB = 1024

        private var instance: MemoryUtils? = null
        fun getInstance(context: Context): MemoryUtils{
            if (instance == null)
                instance = MemoryUtils(context)
            return instance as MemoryUtils
        }

        fun formatToString(value: Long): String {
            val twoDecimalFormat = DecimalFormat("#.##")
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