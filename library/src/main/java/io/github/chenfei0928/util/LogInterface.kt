package io.github.chenfei0928.util

import android.util.Log
import com.google.common.collect.Comparators.min

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-06-02 11:13
 */
interface LogInterface {
    fun v(tag: String, msg: String)
    fun v(tag: String, msg: String, tr: Throwable?)
    fun d(tag: String, msg: String)
    fun d(tag: String, msg: String, tr: Throwable?)
    fun i(tag: String, msg: String)
    fun i(tag: String, msg: String, tr: Throwable?)
    fun w(tag: String, msg: String)
    fun w(tag: String, msg: String, tr: Throwable?)
    fun e(tag: String, msg: String)
    fun e(tag: String, msg: String, tr: Throwable?)
}

internal object SystemLog : LogInterface {

    private fun String.splitByLimit(
        limit: Int = io.github.chenfei0928.util.Log.SYSTEM_LOGCAT_SPLIT_LENGTH
    ) = if (length < limit) arrayOf(this) else
        Array((length / limit) + (if (length % limit == 0) 0 else 1)) {
            substring(it * limit, min((it + 1) * limit, length))
        }

    private inline fun Array<String>.forEachAndReturnLast(
        block: (String, String) -> Unit,
        trBlock: (String, String, Throwable?) -> Unit,
        tag: String,
        tr: Throwable?,
    ) {
        for (i in 0 until size) {
            if (i < size - 1) {
                block(tag, this[i])
            } else {
                trBlock(tag, this[i], tr)
            }
        }
    }

    override fun v(tag: String, msg: String) {
        msg.splitByLimit().forEach { Log.v(tag, it) }
    }

    override fun v(tag: String, msg: String, tr: Throwable?) {
        msg.splitByLimit().forEachAndReturnLast(Log::v, Log::v, tag, tr)
    }

    override fun d(tag: String, msg: String) {
        msg.splitByLimit().forEach { Log.d(tag, it) }
    }

    override fun d(tag: String, msg: String, tr: Throwable?) {
        msg.splitByLimit().forEachAndReturnLast(Log::d, Log::d, tag, tr)
    }

    override fun i(tag: String, msg: String) {
        msg.splitByLimit().forEach { Log.i(tag, it) }
    }

    override fun i(tag: String, msg: String, tr: Throwable?) {
        msg.splitByLimit().forEachAndReturnLast(Log::i, Log::i, tag, tr)
    }

    override fun w(tag: String, msg: String) {
        msg.splitByLimit().forEach { Log.w(tag, it) }
    }

    override fun w(tag: String, msg: String, tr: Throwable?) {
        msg.splitByLimit().forEachAndReturnLast(Log::w, Log::w, tag, tr)
    }

    override fun e(tag: String, msg: String) {
        msg.splitByLimit().forEach { Log.e(tag, it) }
    }

    override fun e(tag: String, msg: String, tr: Throwable?) {
        msg.splitByLimit().forEachAndReturnLast(Log::e, Log::e, tag, tr)
    }
}
