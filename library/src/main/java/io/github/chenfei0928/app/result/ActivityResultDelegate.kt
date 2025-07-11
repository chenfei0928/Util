/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-12-05 14:39
 */
package io.github.chenfei0928.app.result

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.util.WeakHashMap
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private val registerMap =
    WeakHashMap<ActivityResultCaller, MutableList<RegisterLauncherProperty<*, *>>>()

abstract class RegisterLauncherProperty<Host, I> :
    ReadOnlyProperty<Host, ActivityResultLauncher<I>> {
    private lateinit var launcher: ActivityResultLauncher<I>

    final override fun getValue(thisRef: Host, property: KProperty<*>): ActivityResultLauncher<I> {
        return launcher
    }

    internal fun registerLauncher() {
        launcher = register()
    }

    protected abstract fun register(): ActivityResultLauncher<I>
}

fun ActivityResultCaller.registerAllActivityResultLauncher() {
    registerMap[this]?.forEach { it.registerLauncher() }
}

/**
 * 用于[Fragment]等，会在生命周期重启时注销[ActivityResultLauncher]的场景，使用委托提供[ActivityResultLauncher]。
 * 使用此方法注册创建[ActivityResultLauncher]，并在[Fragment.onCreate]中调用[registerAllActivityResultLauncher]方法注册。
 *
 * 不可使用 [LifecycleOwner] 来自动重注册，因为[androidx.lifecycle.LifecycleRegistry.moveToState]
 * 中在 [Lifecycle.Event.ON_DESTROY] 状态时会清空注册的所有的监听器 `observerMap = FastSafeIterableMap()`。
 */
inline fun <Host : ActivityResultCaller, I> Host.registerForActivityResultDelegate(
    crossinline register: () -> ActivityResultLauncher<I>,
): ReadOnlyProperty<Host, ActivityResultLauncher<I>> {
    return registerForActivityResultDelegate(object : RegisterLauncherProperty<Host, I>() {
        override fun register(): ActivityResultLauncher<I> {
            return register()
        }
    })
}

fun <Host : ActivityResultCaller, I> Host.registerForActivityResultDelegate(
    register: RegisterLauncherProperty<Host, I>,
): ReadOnlyProperty<Host, ActivityResultLauncher<I>> {
    return register.also {
        registerMap.getOrPut(this) {
            ArrayList()
        }.add(it)
    }
}
