package com.chenfei.library.util

import android.os.SystemClock

/**
 * 秒表，提供恢复计时、暂停计时、获取当前计时时间
 * Created by MrFeng on 2018/5/8.
 */
class Stopwatch {
    @Volatile
    private var preStartTime: Long = -1
    @Volatile
    private var duration: Long = 0

    /**
     * 开始、恢复计时
     */
    @Synchronized
    fun start() {
        if (preStartTime > 0)
            return
        preStartTime = SystemClock.uptimeMillis()
    }

    /**
     * 暂停计时
     */
    @Synchronized
    fun stop() {
        if (preStartTime <= 0)
            return
        duration += (SystemClock.uptimeMillis() - preStartTime)
        preStartTime = -1
    }

    /**
     * 获取当前计时时间
     */
    @Synchronized
    fun getDuration(): Long {
        return if (preStartTime > 0)
            duration + (SystemClock.uptimeMillis() - preStartTime)
        else
            duration
    }
}
