package io.github.chenfei0928.util.kotlin

import android.os.Debug
import androidx.annotation.Size
import io.github.chenfei0928.library.BuildConfig
import io.github.chenfei0928.util.Log

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-05-15 13:36
 */
class Debug {
    companion object {
        inline fun <T> trace(tracePath: String, block: () -> T): T {
            return if (BuildConfig.DEBUG) {
                Debug.startMethodTracing(tracePath)
                try {
                    block()
                } finally {
                    Debug.stopMethodTracing()
                }
            } else {
                block()
            }
        }

        inline fun <T> countTime(@Size(max = 23) tag: String, block: () -> T): T {
            val l = System.currentTimeMillis()
            val t = block()
            Log.i(tag, "countTime: ${System.currentTimeMillis() - l} ms.")
            return t
        }
    }
}
