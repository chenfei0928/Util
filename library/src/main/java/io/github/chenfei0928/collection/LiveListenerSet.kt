package io.github.chenfei0928.collection

import androidx.collection.ArrayMap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 与生命周期宿主绑定的监听器集合
 *
 * @author chenf()
 * @date 2024-06-27 17:24
 */
class LiveListenerSet<T>(
    private val map: MutableMap<T, Pair<LifecycleOwner, LifecycleEventObserver>?> = ArrayMap()
) : Set<T> {
    override val size: Int
        get() = map.size

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override fun iterator(): Iterator<T> {
        return map.keys.iterator()
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return map.keys.containsAll(elements)
    }

    override fun contains(element: T): Boolean {
        return map.containsKey(element)
    }

    fun observe(owner: LifecycleOwner, element: T) {
        if (map.containsKey(element)) {
            return
        }
        if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            // ignore
            return
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                map.remove(element)
            }
        }
        map[element] = owner to observer
        owner.lifecycle.addObserver(observer)
    }

    fun observeForever(element: T) {
        if (map.containsKey(element)) {
            return
        }
        map.put(element, null)?.let { (owner, observer) ->
            owner.lifecycle.removeObserver(observer)
        }
    }

    fun removeObserver(element: T) {
        map.remove(element)?.let { (owner, observer) ->
            owner.lifecycle.removeObserver(observer)
        }
    }

    fun removeObservers(owner: LifecycleOwner) {
        map.keys.filter {
            map[it]?.first == owner
        }.forEach {
            map.remove(it)
        }
    }
}
