package io.github.chenfei0928.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.util.Log
import java.lang.AutoCloseable

/**
 * @author chenf()
 * @date 2025-02-08 16:36
 */
abstract class BaseServiceConnection(
    private val context: Context,
    private val intent: Intent,
    private val flag: Int,
) : ServiceConnection, AutoCloseable {
    open val tag: String
        get() = context.javaClass.simpleName
    var isBound: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                context.bindService(intent, this, flag)
            } else try {
                context.unbindService(this)
            } catch (e: IllegalArgumentException) {
                Log.e(tag, "bindService: ", e)
            }
        }

    override fun onBindingDied(name: ComponentName) {
        super.onBindingDied(name)
        isBound = false
        isBound = true
    }

    override fun onNullBinding(name: ComponentName?) {
        super.onNullBinding(name)
        isBound = false
    }

    override fun close() {
        isBound = false
    }
}
