package io.github.chenfei0928.os

import android.os.Debug
import androidx.annotation.Size
import io.github.chenfei0928.util.Log
import java.text.DecimalFormat

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-05-15 13:36
 */
class Debug {
    companion object {
        const val msInNs = 1_000_000

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
            val t = block()
            val timeUsed = System.nanoTime() - l
            if (timeUsed < msInNs) {
                // 在一毫秒以内，展示为纳秒
                Log.v(tag, "$msg, countTime: ${
                    DecimalFormat()
                        .apply { applyPattern(",###") }
                        .format(timeUsed)
                } ns.")
            } else {
                // 一毫秒以上，显示为毫秒，保留三位小数
                Log.v(tag, "$msg, countTime: ${
                    DecimalFormat()
                        .apply { applyPattern(",###.###") }
                        .format(timeUsed / msInNs.toFloat())
                } ms.")
            }
            return t
        }
    }
}
