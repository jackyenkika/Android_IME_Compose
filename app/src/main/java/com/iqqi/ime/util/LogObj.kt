package com.iqqi.ime.util

import android.util.Log
import com.iqqi.ime.BuildConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object LogObj {
    private val methodCounterMap = mutableMapOf<String, Int>()

    private const val APP_NAME = BuildConfig.ProjectName
    private const val MSG_TRACE_DEPTH = 2 // 設定msg追蹤深度，預設為2
    private const val MSG_TRACE_DEPTH_DEEP = 20 // 設定log追蹤深度，預設為5

    /**
     * 追蹤log的內容
     * 格式為以下：
     * 1. [ProjectName] - 廠商項目名稱
     * 2. [main] - 執行緒
     * 3. [Mon 01 10 12:13:36 2022] - 編譯時間
     * 4. [class::method()] - 呼叫的位置
     * 5. [43] - 呼叫的程式碼行數
     * 6. [1] - 第幾次呼叫
     * 7. - (msg) - 客製化追蹤內容
     *
     * @param obj 輸入要顯示的log內容
     */

    fun debug(obj: Any) {
        trace(obj, Log.DEBUG)
    }

    fun trace(obj: Any) {
        trace(obj, Log.INFO)
    }

    fun trace(obj: Any, level: Int, isDeepTrace: Boolean = false) {
        if (!BuildConfig.DEBUG && level < Log.INFO) return

        val msg = getMsg(obj, isDeepTrace)
        when (level) {
            Log.ERROR -> Log.e(APP_NAME, msg)
            Log.WARN -> Log.w(APP_NAME, msg)
            Log.DEBUG -> Log.d(APP_NAME, msg)
            Log.VERBOSE -> Log.v(APP_NAME, msg)
            else -> Log.i(APP_NAME, msg)
        }
    }

    fun trace(obj: Any, error: Throwable) {
        if (!BuildConfig.DEBUG) return
        val msg = getMsg(obj)
        Log.e(APP_NAME, msg, error)
    }

    private fun getMsg(obj: Any, isDeepTrace: Boolean = false): String {
        val stack = Thread.currentThread().stackTrace
        val reference = getRefFunctionInfo(stack)
        val refInfo =
            getDeepFunctionInfo(stack, if (isDeepTrace) MSG_TRACE_DEPTH_DEEP else MSG_TRACE_DEPTH)
        return "[${Thread.currentThread().name}] $refInfo [${getRefCounter(reference)}]\n - $obj"
    }

    private fun getTime(): String {
        val calendar = Calendar.getInstance()
        val time = System.currentTimeMillis()
        calendar.timeInMillis = time
        return getTimeByDate(calendar.time, "EE MM dd HH:mm:ss yyyy")
    }

    private fun getCallerElement(
        stackTrace: Array<StackTraceElement>,
        depthOffset: Int = 0
    ): StackTraceElement? {
        var foundLogObj = false
        for (i in stackTrace.indices) {
            val className = stackTrace[i].className
            if (className == LogObj::class.java.name) {
                foundLogObj = true
            } else if (foundLogObj) {
                val targetIndex = i + depthOffset
                return stackTrace.getOrNull(targetIndex)
            }
        }
        return null
    }

    private fun getRefFunctionInfo(stack: Array<StackTraceElement>): String {
        val element = getCallerElement(stack)
        return element?.let {
            "[${it.className}::${it.methodName}] [${it.lineNumber}]"
        } ?: "[Unknown::Unknown] [-1]"
    }

    private fun getDeepFunctionInfo(stack: Array<StackTraceElement>, depthOffset: Int): String {
        val sb = StringBuilder()
        for (i in 0 until depthOffset) {
            val element = getCallerElement(stack, i)
            sb.insert(0, element?.let {
                "[${it.className}::${it.methodName}] [${it.lineNumber}] "
            } ?: "[Unknown::Unknown] [-1] ")
        }
        return sb.toString()
    }

    private fun getRefCounter(reference: String): String {
        val count = methodCounterMap.getOrDefault(reference, 0) + 1
        methodCounterMap[reference] = count
        return count.toString()
    }

    /**
     * 獲取指定output格式時間字串
     */
    private fun getTimeByDate(date: Date, outputFormat: String = "yyyyMMddHHmmss"): String {
        // 创建一个SimpleDateFormat对象，定义日期格式
        val sdf = SimpleDateFormat(outputFormat, Locale.getDefault())
        // 格式化Date对象为字符串
        return sdf.format(date)
    }

}