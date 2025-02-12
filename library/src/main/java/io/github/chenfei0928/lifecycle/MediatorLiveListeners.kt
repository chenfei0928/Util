package io.github.chenfei0928.lifecycle

import androidx.collection.ArrayMap

/**
 * @author chenf()
 * @date 2025-02-12 16:15
 */
open class MediatorLiveListeners<ThisObserver> : LiveListeners<ThisObserver>() {
    private val sources: MutableMap<LiveListeners<*>, Source<*>> = ArrayMap()

    fun <Observer> addSource(sourceLiveListeners: LiveListeners<Observer>, observer: Observer) {
        if (sourceLiveListeners in sources) {
            return
        }
        val source = Source(sourceLiveListeners, observer)
        sources.put(sourceLiveListeners, source)?.let {
            it.active = false
        }
        if (hasActiveObserver()) {
            source.active = true
        }
    }

    fun <Observer> removeSource(source: LiveListeners<Observer>) {
        sources.remove(source)?.let {
            it.active = false
        }
    }

    override fun onActive() {
        sources.forEach { it.value.active = true }
    }

    override fun onInactive() {
        sources.forEach { it.value.active = false }
    }

    private class Source<Observer>(
        private val liveListeners: LiveListeners<Observer>,
        private val observer: Observer,
    ) {
        var active: Boolean = false
            set(value) {
                if (field == value)
                    return
                if (value) {
                    liveListeners.observeForever(observer)
                } else {
                    liveListeners.removeObserver(observer)
                }
            }
    }
}
