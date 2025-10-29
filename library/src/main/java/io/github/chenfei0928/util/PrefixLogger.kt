package io.github.chenfei0928.util

/**
 * @author chenf()
 * @date 2025-10-29 11:30
 */
class PrefixLogger(
    private val prefix: String,
) : LogInterface {
    override fun v(tag: String, msg: String, tr: Throwable?) {
        Log.v(prefix + tag, msg, tr)
    }

    override fun d(tag: String, msg: String, tr: Throwable?) {
        Log.d(prefix + tag, msg, tr)
    }

    override fun i(tag: String, msg: String, tr: Throwable?) {
        Log.i(prefix + tag, msg, tr)
    }

    override fun w(tag: String, msg: String, tr: Throwable?) {
        Log.w(prefix + tag, msg, tr)
    }

    override fun e(tag: String, msg: String, tr: Throwable?) {
        Log.e(prefix + tag, msg, tr)
    }
}
