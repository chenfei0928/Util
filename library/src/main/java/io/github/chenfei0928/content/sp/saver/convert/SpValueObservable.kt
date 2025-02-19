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
class SpValueObservable<SpSaver : AbsSpSaver<SpSaver, *, *>, V>(
    override val saver: AbsSpSaver.Delegate<SpSaver, V>,
) : LiveListeners<(V) -> Unit>(),
    AbsSpSaver.Delegate<SpSaver, V> by saver,
    AbsSpSaver.Decorate<SpSaver, V> {

    override fun setValue(thisRef: SpSaver, property: KProperty<*>, value: V) {
        saver.setValue(thisRef, property, value)
        if (hasActiveObserver()) {
            forEach { it(value) }
        }
    }

    fun onLocalStorageChange(thisRef: SpSaver, property: KProperty<*>) {
        if (hasActiveObserver()) {
            val value = getValue(thisRef, property)
            forEach { it(value) }
        }
    }

    override fun toString(): String {
        return "SpValueObservable(saver=$saver)"
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver, *, *>, V> find(
            outDelegate: AbsSpSaver.Delegate<SpSaver, V>
        ): SpValueObservable<SpSaver, V>? = AbsSpSaver.Decorate.findByType(outDelegate)
    }
}
