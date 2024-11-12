package io.github.chenfei0928.os

import android.os.Debug
import androidx.annotation.Size
import io.github.chenfei0928.util.Log
import java.text.DecimalFormat
import java.text.FieldPosition

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-05-15 13:36
 */
object Debug {
    const val msInNs = 1_000_000
    private val fieldPosition = FieldPosition(0)

    inline fun <T> traceTime(
        tag: String,
        tracePath: String,
        msg: String = tracePath,
        block: () -> T
    ) = trace(tracePath) {
        countTime(tag, msg, block)
    }

    inline fun <T> trace(tracePath: String, block: () -> T): T {
        Debug.startMethodTracing(tracePath)
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
            logCountTime(tag, msg, l)
        }
    }

    fun logCountTime(tag: String, msg: String, nanoTime: Long) {
        val timeUsed = System.nanoTime() - nanoTime
        if (timeUsed < msInNs) {
            // 在一毫秒以内，展示为纳秒
            Log.v(tag, StringBuffer().apply {
                append(msg)
                append(", countTime: ")
                DecimalFormat(",###")
                    .format(timeUsed, this, fieldPosition)
                append(" ns.")
            }.toString())
        } else {
            // 一毫秒以上，显示为毫秒，保留三位小数
            Log.v(tag, StringBuffer().apply {
                append(msg)
                append(", countTime: ")
                DecimalFormat(",###.###")
                    .format(timeUsed / msInNs.toFloat(), this, fieldPosition)
                append(" ms.")
            }.toString())
        }
    }
}
