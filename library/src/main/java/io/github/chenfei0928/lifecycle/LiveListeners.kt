package io.github.chenfei0928.lifecycle

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
open class LiveListeners<Observer> : Iterable<Observer>, ILiveListener<Observer> {
    private val map: MutableMap<Observer, BindWrapper<Observer>> = ArrayMap()
    private var activeObserverCount = 0
        set(value) {
            val oldValue = field
            if (oldValue == value)
                return
            field = value
            if (oldValue == 0 && value == 1) {
                onActive()
            } else if (oldValue == 1 && value == 0) {
                onInactive()
            }
        }

    fun hasActiveObserver(): Boolean = activeObserverCount > 0

    override fun iterator(): Iterator<Observer> {
        return map.values.mapNotNull { bind ->
            bind.observer.takeIf { bind.active }
        }.iterator()
    }

    override fun observe(owner: LifecycleOwner, state: Lifecycle.State, observer: Observer) {
        if (observer in map || owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            // ignore
            return
        }
        val eventObserver = ObserveLifecycleBind(owner, state, observer)
        map.put(observer, eventObserver)
        owner.lifecycle.addObserver(eventObserver)
    }

    override fun observeForever(observer: Observer) {
        if (map.containsKey(observer)) {
            return
        }
        map.put(observer, ForeverBind(observer))
    }

    override fun removeObserver(observer: Observer) {
        map.remove(observer)?.let { eventObserver ->
            if (eventObserver is LiveListeners.ObserveLifecycleBind) {
                eventObserver.owner.lifecycle.removeObserver(eventObserver)
            }
            eventObserver.onRemoved()
            onObserverRemoved(observer)
        }
    }

    override fun removeObservers(owner: LifecycleOwner) {
        map.forEach { (observer, bind) ->
            if (bind is LiveListeners.ObserveLifecycleBind && bind.owner === owner)
                removeObserver(observer)
        }
    }

    protected open fun onObserverActiveChanged(observer: Observer, isActive: Boolean) {
        // noop
    }

    protected open fun onObserverRemoved(observer: Observer) {
        // noop
    }

    /**
     * Called when the number of active observers change from 0 to 1.
     */
    protected open fun onActive() {
        // noop
    }

    /**
     * Called when the number of active observers change from 1 to 0.
     *
     * You can check if there are observers via {@link #hasObservers()}.
     */
    protected open fun onInactive() {
        // noop
    }

    private sealed interface BindWrapper<Observer> {
        val active: Boolean
        val observer: Observer

        fun onRemoved()
    }

    private inner class ForeverBind(
        override val observer: Observer
    ) : BindWrapper<Observer> {
        override val active: Boolean = true

        init {
            activeObserverCount++
        }

        override fun onRemoved() {
            activeObserverCount--
        }
    }

    private inner class ObserveLifecycleBind(
        val owner: LifecycleOwner,
        private val state: Lifecycle.State,
        override val observer: Observer,
    ) : LifecycleEventObserver, BindWrapper<Observer> {
        override var active: Boolean = false
            set(value) {
                if (field == value)
                    return
                field = value
                if (value) {
                    activeObserverCount++
                } else {
                    activeObserverCount--
                }
                onObserverActiveChanged(observer, field)
            }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            active = owner.lifecycle.currentState >= state
            if (event == Lifecycle.Event.ON_DESTROY) {
                onObserverActiveChanged(observer, false)
                removeObserver(observer)
            }
        }

        override fun onRemoved() {
            active = false
        }
    }
}
