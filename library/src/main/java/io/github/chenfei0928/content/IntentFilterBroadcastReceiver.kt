package io.github.chenfei0928.content

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.chenfei0928.lifecycle.isAlive

/**
 * 跟随生命周期随动自动反注册的广播接收者
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-12-08 14:27
 */
abstract class IntentFilterBroadcastReceiver(
    private vararg val actions: String
) : BroadcastReceiver() {
    private val intentFilter = IntentFilter().apply {
        for (action in actions) {
            addAction(action)
        }
    }

    open fun register(context: Context, owner: LifecycleOwner) {
        if (!owner.lifecycle.isAlive) {
            return
        }
        register(context)
        owner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                context.unregisterReceiver(this@IntentFilterBroadcastReceiver)
            }
        })
    }

    open fun register(context: Context) {
        ContextCompat.registerReceiver(
            context, this, intentFilter, ContextCompat.RECEIVER_EXPORTED
        )
    }

    open fun registerLocal(context: Context, owner: LifecycleOwner) {
        if (!owner.lifecycle.isAlive) {
            return
        }
        registerLocal(context)
        owner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                LocalBroadcastManager.getInstance(context)
                    .unregisterReceiver(this@IntentFilterBroadcastReceiver)
            }
        })
    }

    open fun registerLocal(context: Context) {
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(this, intentFilter)
    }
}
