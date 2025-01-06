package io.github.chenfei0928.os

import android.os.Debug
import io.github.chenfei0928.annotation.NoSideEffects
import androidx.annotation.Size
import io.github.chenfei0928.text.appendFormat
import io.github.chenfei0928.util.Log
import io.github.chenfei0928.util.NonnullPools
import io.github.chenfei0928.util.use
import java.text.DecimalFormat

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-05-15 13:36
 */
object Debug {
    const val msInNs = 1_000_000
    private val logCountTimeFormat =
        NonnullPools.SimplePool<Pair<DecimalFormat, DecimalFormat>>(1) {
            DecimalFormat(",###") to DecimalFormat(",###.###")
        }

    inline fun <T> traceAndTime(
        tag: String,
        tracePath: String,
        msg: String = tracePath,
        block: () -> T
    ): T {
        // 追加时间戳后缀，避免AndroidStudio Profiler对trace进行的缓存
        val l = System.currentTimeMillis()
        Log.v(tag, "$msg, currentTimeMillis: $l")
        Debug.startMethodTracing("${tracePath}_$l")
        val nanoTime = System.nanoTime()
        return try {
            block()
        } finally {
            val timeUsed = System.nanoTime() - nanoTime
            Debug.stopMethodTracing()
            logCountTime(tag, msg, timeUsed)
        }
    }

    inline fun <T> trace(tracePath: String, block: () -> T): T {
        // 追加时间戳后缀，避免AndroidStudio Profiler对trace进行的缓存
        val l = System.currentTimeMillis()
        Debug.startMethodTracing("${tracePath}_$l")
        return try {
            block()
        } finally {
            Debug.stopMethodTracing()
        }
    }

    inline fun <T> countTime(
        @Size(max = 23) tag: String,
        msg: String = "",
        block: () -> T
    ): T {
        Log.v(tag, "$msg, currentTimeMillis: ${System.currentTimeMillis()}")
        val l = System.nanoTime()
        return try {
            block()
        } finally {
            logCountTime(tag, msg, System.nanoTime() - l)
        }
    }

    @NoSideEffects
    fun logCountTime(
        tag: String,
        msg: String,
        timeUsed: Long
    ): Unit = logCountTimeFormat.use { format ->
        if (timeUsed < msInNs) {
            // 在一毫秒以内，展示为纳秒
            Log.v(tag, StringBuffer().apply {
                append(msg)
                append(", countTime: ")
                appendFormat(format.first, timeUsed)
                append(" ns.")
            }.toString())
        } else {
            // 一毫秒以上，显示为毫秒，保留三位小数
            Log.v(tag, StringBuffer().apply {
                append(msg)
                append(", countTime: ")
                appendFormat(format.second, timeUsed / msInNs.toFloat())
                append(" ms.")
            }.toString())
        }
    }
}
