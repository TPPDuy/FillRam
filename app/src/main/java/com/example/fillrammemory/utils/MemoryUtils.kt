package com.example.fillrammemory.utils

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.fillrammemory.classes.AppInfo
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class MemoryUtils(var context: Context) {

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

    private fun getAppInfo(packageName: String): AppInfo?{
        return try{
            if (packageName == context.packageName) return null //eliminate current app

            val packageManager = context.packageManager
            val app = packageManager.getApplicationInfo(packageName, 0)
            if ((app.flags.and(ApplicationInfo.FLAG_STOPPED)) != 0) null
            else AppInfo(
                packageName,
                packageManager.getApplicationLabel(app).toString(),
                packageManager.getApplicationIcon(app),
                0
            )
        } catch (ex: PackageManager.NameNotFoundException){
            null
        }

    }
    fun getRunningAppPreLollipop(): ArrayList<AppInfo>{
        val result = ArrayList<AppInfo>()
        val packageManager = context.packageManager
        val runningAppProcesses = activityManager.runningAppProcesses

        for (appProcessInfo in runningAppProcesses){
            val appInfo = packageManager.getApplicationInfo(
                appProcessInfo.processName,
                PackageManager.GET_META_DATA
            )
            val label = packageManager.getApplicationLabel(appInfo)
            val icon = packageManager.getApplicationIcon(appInfo)
            val pid = appProcessInfo.pid
            val memUsage = activityManager.getProcessMemoryInfo(IntArray(1).apply { set(0, pid) })[0].totalPss*1000.toLong()
            result.add(
                AppInfo(
                    packageManager.getPackageInfo(
                        appProcessInfo.processName,
                        PackageManager.GET_META_DATA
                    ).packageName, label.toString(), icon, memUsage
                )
            )
        }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun getRunningAppPostLollipop() : ArrayList<AppInfo>{
        val result = ArrayList<AppInfo>()
        val calendar: Calendar = Calendar.getInstance()
        val endTime: Long = calendar.timeInMillis
        calendar.add(Calendar.SECOND, -5)
        val startTime: Long = calendar.timeInMillis
        val usm: UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        for (us in usageStatsList){
            Log.d("TIME USED", us.lastTimeUsed.toString())
           if (us.lastTimeUsed > 100000) {
                val appInfo = getAppInfo(us.packageName)
                appInfo?.lastTimeUsed = us.lastTimeUsed
                if (appInfo != null) result.add(appInfo)
           }
        }
        return result
    }

    fun killProcessByPackageName(packageName: String){
        activityManager.killBackgroundProcesses(packageName)
    }

    private fun convertMBToBytes(mbValue: Long): Long {
        return (mbValue * MBToB).toLong();
    }
    private fun convertGBToBytes(mbValue: Long): Long {
        return (mbValue * GBToB).toLong();
    }

    private fun convertKBToBytes(kbValue: Long): Long {
        return (kbValue * KBToB).toLong();
    }

    fun convertValueToBytes(value: Long, unit: String) : Long {
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

    fun isAvailableAdded(value: Long, unit: String) : Boolean {
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

        val availableMem = memoryInfo.availMem.div(1024 * 1024).div(MBToKB).toDouble()
        Log.d("TAG", availableMem.toString())

        if(convertValue <= availableMem) {
            return true
        }
        return false
    }

    companion object{
        private const val MBToKB = 1024.0
        private const val GBToKB = 1024.0 * 1024.0
        private const val MBToB = 1024.0 * 1024.0
        private const val GBToB = 1024.0 * 1024.0 * 1024.0
        private const val KBToB = 1024.0

        private var instance: MemoryUtils? = null

        fun getInstance(context: Context): MemoryUtils{
            if (instance == null)
                instance = MemoryUtils(context)
            return instance as MemoryUtils
        }

        fun formatToString(byteValue: Long): String {
            val twoDecimalFormat = DecimalFormat("#.##")
            val mbValue = byteValue.div(KBToB).div(MBToKB)
            val gbValue = byteValue.div(KBToB).div(GBToKB)
            return when {
                gbValue >= 1.0 -> {
                    twoDecimalFormat.format(gbValue).plus("GB")
                }
                mbValue >= 1.0 -> {
                    twoDecimalFormat.format(mbValue).plus("MB")
                }
                else -> {
                    twoDecimalFormat.format(byteValue).plus("KB")
                }
            }
        }

        /*
        * type:
        * 1 - Total
        * 2 - Free
        * *//*
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
        }*/

    }
}