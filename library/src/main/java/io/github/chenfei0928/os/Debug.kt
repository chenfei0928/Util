package io.github.chenfei0928.os

import android.os.Debug
import androidx.annotation.Size
import io.github.chenfei0928.annotation.NoSideEffects
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
    // 毫秒内有多少纳秒
    const val MILLIS_IN_NANOS = 1_000_000
    private val logCountTimeFormat =
        NonnullPools.SimplePool<Pair<DecimalFormat, DecimalFormat>>(1) {
            DecimalFormat(",###") to DecimalFormat(",###.###")
        }

    inline fun <T> traceAndTime(
        tag: String,
        tracePath: String,
        msg: String = tracePath,
        block: (StopWatch) -> T
    ): T {
        // 追加时间戳后缀，避免AndroidStudio Profiler对trace进行的缓存
        Log.v(tag, "$msg, nanoTime: ${System.nanoTime()}")
        val watch = object : StopWatch.DebugTrace() {
            override val tracePath: String
                get() = "${tracePath}_${System.nanoTime()}"
        }
        watch.start()
        return try {
            block(watch)
        } finally {
            watch.stop()
            logCountTime(tag, msg, watch)
        }
    }

    inline fun <T> trace(tracePath: String, block: (StopWatch) -> T): T {
        // 追加时间戳后缀，避免AndroidStudio Profiler对trace进行的缓存
        val watch = object : StopWatch.DebugTrace() {
            override val tracePath: String
                get() = "${tracePath}_${System.nanoTime()}"
        }
        watch.start()
        return try {
            block(watch)
        } finally {
            watch.stop()
        }
    }

    inline fun <T> countTime(
        @Size(max = 23) tag: String,
        msg: String = "",
        block: (StopWatch) -> T
    ): T {
        Log.v(tag, "$msg, nanoTime: ${System.nanoTime()}")
        val watch = StopWatch.Timestamps()
        watch.start()
        return try {
            block(watch)
        } finally {
            watch.stop()
            logCountTime(tag, msg, watch)
        }
    }

    @NoSideEffects
    fun logCountTime(
        tag: String,
        msg: String,
        timeUsed: StopWatch.Internal
    ): Unit = logCountTimeFormat.use { format ->
        Log.v(tag, StringBuffer().apply {
            append(msg)
            append(", countTime: ")
            for (i in 0 until timeUsed.nextIndex step 2) {
                val timeUsed = timeUsed.timestamps[i + 1] - timeUsed.timestamps[i]
                if (timeUsed < MILLIS_IN_NANOS) {
                    // 在一毫秒以内，展示为纳秒
                    appendFormat(format.first, timeUsed)
                    append(" ns, ")
                } else {
                    // 一毫秒以上，显示为毫秒，保留三位小数
                    appendFormat(format.second, timeUsed / MILLIS_IN_NANOS.toFloat())
                    append(" ms, ")
                }
            }
            replace(length - 2, length, ".")
        }.toString())
    }

    sealed interface StopWatch {
        fun record()

        sealed class Internal(
            size: Int
        ) : StopWatch {
            internal var nextIndex = 0
            internal val timestamps: LongArray = LongArray(size)
            abstract fun start()
            abstract fun stop()
        }

        class Timestamps(
            size: Int = 32
        ) : Internal(size) {
            override fun start() {
                timestamps[nextIndex] = System.nanoTime()
                nextIndex++
            }

            override fun record() {
                timestamps[nextIndex] = System.nanoTime()
                nextIndex++
                timestamps[nextIndex] = System.nanoTime()
                nextIndex++
            }

            override fun stop() {
                timestamps[nextIndex] = System.nanoTime()
                nextIndex++
            }
        }

        abstract class DebugTrace(
            size: Int = 32
        ) : Internal(size) {
            abstract val tracePath: String

            override fun start() {
                Debug.startMethodTracing(tracePath)
                timestamps[nextIndex] = System.nanoTime()
                nextIndex++
            }

            override fun record() {
                timestamps[nextIndex] = System.nanoTime()
                nextIndex++
                Debug.stopMethodTracing()
                Debug.startMethodTracing(tracePath)
                timestamps[nextIndex] = System.nanoTime()
                nextIndex++
            }

            override fun stop() {
                timestamps[nextIndex] = System.nanoTime()
                nextIndex++
                Debug.stopMethodTracing()
            }
        }
    }
}
