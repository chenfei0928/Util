package io.github.chenfei0928.content.sp.saver.convert

import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.lifecycle.LiveListeners
import kotlin.reflect.KProperty

/**
 * 字段值更新时的监听对象
 *
 * @author chenf()
 * @date 2025-02-11 17:22
 */
class SpValueObservable<SpSaver : AbsSpSaver<SpSaver, *, *>, T>(
    override val saver: AbsSpSaver.Delegate<SpSaver, T>,
) : LiveListeners<(T) -> Unit>(), AbsSpSaver.Delegate<SpSaver, T> by saver,
    AbsSpSaver.Decorate<SpSaver, T> {

    override fun setValue(thisRef: SpSaver, property: KProperty<*>, value: T) {
        saver.setValue(thisRef, property, value)
        if (hasActiveObserver()) {
            forEach { it(value) }
        }
    }
}
