package io.github.chenfei0928.content.sp.saver.convert

import io.github.chenfei0928.lifecycle.LiveListeners
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import kotlin.reflect.KProperty

/**
 * 字段值更新时的监听对象
 *
 * @author chenf()
 * @date 2025-02-11 17:22
 */
class SpValueObservable<SpSaver : AbsSpSaver<SpSaver, *, *>, T>(
    internal val saver: AbsSpSaver.AbsSpDelegate<SpSaver, T>,
) : LiveListeners<(T) -> Unit>(), AbsSpSaver.AbsSpDelegate<SpSaver, T> by saver {

    override fun setValue(thisRef: SpSaver, property: KProperty<*>, value: T) {
        saver.setValue(thisRef, property, value)
        if (hasActiveObserver()) {
            forEach { it(value) }
        }
    }
}
