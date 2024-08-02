/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-12-05 14:39
 */
package io.github.chenfei0928.app.result

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import java.util.WeakHashMap
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private val registerMap =
    WeakHashMap<ActivityResultCaller, MutableList<RegisterLauncherProperty<*, *>>>()

private interface RegisterLauncherProperty<Host, I> :
    ReadOnlyProperty<Host, ActivityResultLauncher<I>> {
    fun register()
}

fun ActivityResultCaller.registerAllActivityResultLauncher() {
    registerMap[this]?.forEach { it.register() }
}

/**
 * 用于[Fragment]等，会在生命周期重启时注销[ActivityResultLauncher]的场景，使用委托提供[ActivityResultLauncher]。
 * 使用此方法注册创建[ActivityResultLauncher]，并在[Fragment.onCreate]中调用[registerAllActivityResultLauncher]方法注册。
 */
fun <Host : ActivityResultCaller, I> Host.registerForActivityResultDelegate(
    register: () -> ActivityResultLauncher<I>,
): ReadOnlyProperty<Host, ActivityResultLauncher<I>> {
    return object : RegisterLauncherProperty<Host, I> {
        private lateinit var launcher: ActivityResultLauncher<I>

        override fun getValue(
            thisRef: Host,
            property: KProperty<*>,
        ): ActivityResultLauncher<I> {
            return launcher
        }

        override fun register() {
            launcher = register()
        }
    }.also {
        registerMap.getOrPut(this) {
            ArrayList()
        }.add(it)
    }
}
