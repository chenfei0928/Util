package io.github.chenfei0928.util

import androidx.annotation.Size

/**
 * @author chenf()
 * @date 2025-10-29 11:30
 */
class PrefixLogger(
    @all:Size(max = 3) private val prefix: String,
) : LogInterface {
    override fun v(@Size(max = 20) tag: String, msg: String, tr: Throwable?) {
        Log.v(prefix + tag, msg, tr)
    }

    override fun d(@Size(max = 20) tag: String, msg: String, tr: Throwable?) {
        Log.d(prefix + tag, msg, tr)
    }

    override fun i(@Size(max = 20) tag: String, msg: String, tr: Throwable?) {
        Log.i(prefix + tag, msg, tr)
    }

    override fun w(@Size(max = 20) tag: String, msg: String, tr: Throwable?) {
        Log.w(prefix + tag, msg, tr)
    }

    override fun e(@Size(max = 20) tag: String, msg: String, tr: Throwable?) {
        Log.e(prefix + tag, msg, tr)
    }
}
