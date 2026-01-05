package io.github.chenfei0928.util

import android.os.DeadSystemException
import android.util.Log
import androidx.annotation.Size
import com.google.common.collect.Comparators.min
import io.github.chenfei0928.lang.printStackTrace

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
        tag: String,
        msg: String,
        tr: Throwable?,
    ) {
        val limit: Int = io.github.chenfei0928.util.Log.SYSTEM_LOGCAT_SPLIT_LENGTH
        val trMsg = if (tr == null) msg else run {
            var t: Throwable? = tr
            while (t != null) {
                if (t is java.net.UnknownHostException) {
                    return@run msg
                }
                if (t is DeadSystemException) {
                    return@run "$msg\nDeadSystemException: The system died; earlier logs will point to the root cause"
                }
                t = t.cause
            }
            StringBuilder().also {
                it.append(msg)
                it.appendLine()
                tr.printStackTrace(it)
            }.toString()
        }
        if (trMsg.length <= limit) {
            block(tag, trMsg)
            return
        }
        val size = (trMsg.length / limit) + (if (trMsg.length % limit == 0) 0 else 1)
        for (index in 0 until size) {
            val limitMsg = trMsg.substring(index * limit, min((index + 1) * limit, trMsg.length))
            block(tag, limitMsg)
        }
    }

    override fun v(tag: String, msg: String, tr: Throwable?) {
        splitAndLogLongMessage(Log::v, tag, msg, tr)
    }

    override fun d(tag: String, msg: String, tr: Throwable?) {
        splitAndLogLongMessage(Log::d, tag, msg, tr)
    }

    override fun i(tag: String, msg: String, tr: Throwable?) {
        splitAndLogLongMessage(Log::i, tag, msg, tr)
    }

    override fun w(tag: String, msg: String, tr: Throwable?) {
        splitAndLogLongMessage(Log::w, tag, msg, tr)
    }

    override fun e(tag: String, msg: String, tr: Throwable?) {
        splitAndLogLongMessage(Log::e, tag, msg, tr)
    }
}
