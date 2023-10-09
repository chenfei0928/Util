package io.github.chenfei0928.util

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import java.util.WeakHashMap
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2022-12-05 14:39
 */
private val registerMap = WeakHashMap<Any, MutableList<RegisterLauncherProperty<*, *>>>()

private interface RegisterLauncherProperty<Host, I> :
    ReadOnlyProperty<Host, ActivityResultLauncher<I>> {
    fun register()
}

fun ActivityResultCaller.registerAllActivityResultLauncher() {
    registerMap[this]?.forEach { it.register() }
}

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
