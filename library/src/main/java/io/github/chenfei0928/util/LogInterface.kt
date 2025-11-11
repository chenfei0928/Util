package io.github.chenfei0928.util

import android.util.Log
import androidx.annotation.Size
import com.google.common.collect.Comparators.min

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-06-02 11:13
 */
interface LogInterface {
    fun v(@Size(max = 23) tag: String, msg: String) = v(tag, msg, null)
    fun v(@Size(max = 23) tag: String, msg: String, tr: Throwable?)
    fun d(@Size(max = 23) tag: String, msg: String) = d(tag, msg, null)
    fun d(@Size(max = 23) tag: String, msg: String, tr: Throwable?)
    fun i(@Size(max = 23) tag: String, msg: String) = i(tag, msg, null)
    fun i(@Size(max = 23) tag: String, msg: String, tr: Throwable?)
    fun w(@Size(max = 23) tag: String, msg: String) = w(tag, msg, null)
    fun w(@Size(max = 23) tag: String, msg: String, tr: Throwable?)
    fun e(@Size(max = 23) tag: String, msg: String) = e(tag, msg, null)
    fun e(@Size(max = 23) tag: String, msg: String, tr: Throwable?)
}

internal object SystemLog : LogInterface {

    private inline fun splitAndLogLongMessage(
        block: (String, String) -> Unit,
        trBlock: (String, String, Throwable) -> Unit,
        tag: String,
        msg: String,
        tr: Throwable?,
    ) {
        val limit: Int = io.github.chenfei0928.util.Log.SYSTEM_LOGCAT_SPLIT_LENGTH
        if (msg.length <= limit) {
            if (tr == null) block(tag, msg)
            else trBlock(tag, msg, tr)
            return
        }
        val size = (msg.length / limit) + (if (msg.length % limit == 0) 0 else 1)
        for (index in 0 until size) {
            val limitMsg = msg.substring(index * limit, min((index + 1) * limit, msg.length))
            if (tr == null) {
                block(tag, limitMsg)
            } else if (index < size - 1) {
                block(tag, limitMsg)
            } else {
                trBlock(tag, limitMsg, tr)
            }
        }
    }

    override fun v(tag: String, msg: String, tr: Throwable?) {
        splitAndLogLongMessage(Log::v, Log::v, tag, msg, tr)
    }

    override fun d(tag: String, msg: String, tr: Throwable?) {
        splitAndLogLongMessage(Log::d, Log::d, tag, msg, tr)
    }

    override fun i(tag: String, msg: String, tr: Throwable?) {
        splitAndLogLongMessage(Log::i, Log::i, tag, msg, tr)
    }

    override fun w(tag: String, msg: String, tr: Throwable?) {
        splitAndLogLongMessage(Log::w, Log::w, tag, msg, tr)
    }

    override fun e(tag: String, msg: String, tr: Throwable?) {
        splitAndLogLongMessage(Log::e, Log::e, tag, msg, tr)
    }
}
