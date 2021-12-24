package io.github.chenfei0928.content

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.util.kotlin.isAlive

/**
 * 跟随生命周期随动自动反注册的广播接收者
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-12-08 14:27
 */
abstract class IntentFilterBroadcastReceiver(
    private vararg val actions: String
) : BroadcastReceiver() {

    open fun register(context: Context, owner: LifecycleOwner) {
        if (owner.lifecycle.isAlive) {
            register(context)
            owner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        context.unregisterReceiver(this@IntentFilterBroadcastReceiver)
                    }
                }
            })
        }
    }

    open fun register(context: Context) {
        context.registerReceiver(this, IntentFilter().apply {
            for (action in actions) {
                addAction(action)
            }
        })
    }
}
