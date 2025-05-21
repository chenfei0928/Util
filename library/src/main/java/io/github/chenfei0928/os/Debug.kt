package io.github.chenfei0928.os

import android.os.Debug
import android.view.Choreographer
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

    fun traceAndTimeFrameTime(
        tag: String,
        tracePath: String = "${tag}_traceFrameTime",
        msg: String = tracePath,
    ) {
        // 追加时间戳后缀，避免AndroidStudio Profiler对trace进行的缓存
        val l = System.nanoTime()
        Log.v(tag, "$msg, nanoTime: $l")
        Debug.startMethodTracing("${tracePath}_$l")
        Choreographer.getInstance().postFrameCallback {
            val endTime = System.nanoTime()
            Debug.stopMethodTracing()
            logCountTime(tag, msg, StopWatch.NoMoreRecord(l, endTime))
        }
    }

    inline fun <T> traceAndTime(
        tag: String,
        tracePath: String,
        msg: String = tracePath,
        block: (StopWatch) -> T
    ): T {
        // 追加时间戳后缀，避免AndroidStudio Profiler对trace进行的缓存
        Log.v(tag, "$msg, nanoTime: ${System.nanoTime()}")
        val watch = StopWatch.DebugTrace(tracePath)
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
        val watch = StopWatch.DebugTrace(tracePath)
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
        timeUsed: StopWatch.LogCountTime
    ): Unit = logCountTimeFormat.use { format ->
        Log.v(tag, StringBuffer().apply {
            append(msg)
            append(", countTimes: ")
            for (i in 0 until timeUsed.nextIndex step 2) {
                timeUsed.names[i / 2]?.let {
                    append(it)
                    append('_')
                }
                val timeUsed = timeUsed.nanoTimestamps[i + 1] - timeUsed.nanoTimestamps[i]
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
        /**
         * 记录一个耗时信息
         *
         * @param name 调用此方法前的代码的标记标签
         */
        fun record(name: String? = null)

        sealed interface LogCountTime {
            val nextIndex: Int
            val nanoTimestamps: LongArray
            val names: Array<String?>
        }

        sealed class Internal(
            size: Int
        ) : StopWatch, LogCountTime {
            override var nextIndex = 0
            override val nanoTimestamps: LongArray = LongArray(size)
            override val names: Array<String?> = Array<String?>(size) { null }
            abstract fun start()
            abstract fun stop()
        }

        class NoMoreRecord(
            startTimestamp: Long,
            endTimestamp: Long,
        ) : LogCountTime {
            override val nextIndex: Int = 2
            override val nanoTimestamps: LongArray = longArrayOf(
                startTimestamp, endTimestamp
            )
            override val names: Array<String?> = arrayOf(null)
        }

        class Timestamps(
            size: Int = 32
        ) : Internal(size) {
            override fun start() {
                nanoTimestamps[nextIndex] = System.nanoTime()
                nextIndex++
            }

            override fun record(name: String?) {
                names[nextIndex / 2] = name
                nanoTimestamps[nextIndex] = System.nanoTime()
                nextIndex++
                nanoTimestamps[nextIndex] = System.nanoTime()
                nextIndex++
            }

            override fun stop() {
                names[nextIndex / 2] = ""
                nanoTimestamps[nextIndex] = System.nanoTime()
                nextIndex++
            }
        }

        class DebugTrace(
            private val tracePathPrefix: String,
            size: Int = 32
        ) : Internal(size) {
            private val tracePath: String
                get() = "${tracePathPrefix}_${System.nanoTime()}"

            override fun start() {
                Debug.startMethodTracing(tracePath)
                nanoTimestamps[nextIndex] = System.nanoTime()
                nextIndex++
            }

            override fun record(name: String?) {
                names[nextIndex / 2] = name
                nanoTimestamps[nextIndex] = System.nanoTime()
                nextIndex++
                Debug.stopMethodTracing()
                Debug.startMethodTracing(tracePath)
                nanoTimestamps[nextIndex] = System.nanoTime()
                nextIndex++
            }

            override fun stop() {
                names[nextIndex / 2] = ""
                nanoTimestamps[nextIndex] = System.nanoTime()
                nextIndex++
                Debug.stopMethodTracing()
            }
        }
    }
}
