package io.github.chenfei0928.util

import android.util.Log

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
    override fun v(tag: String, msg: String) {
        Log.v(tag, msg)
    }

    override fun v(tag: String, msg: String, tr: Throwable?) {
        Log.v(tag, msg, tr)
    }

    override fun d(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    override fun d(tag: String, msg: String, tr: Throwable?) {
        Log.d(tag, msg, tr)
    }

    override fun i(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    override fun i(tag: String, msg: String, tr: Throwable?) {
        Log.i(tag, msg, tr)
    }

    override fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    override fun w(tag: String, msg: String, tr: Throwable?) {
        Log.w(tag, msg, tr)
    }

    override fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    override fun e(tag: String, msg: String, tr: Throwable?) {
        Log.e(tag, msg, tr)
    }
}
