/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-05 22:08
 */

@file:JvmName("LiveDataCf0928Util")

package androidx.lifecycle

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 等待并接收值，其值也有可能会为空（其允许设置[LiveData.setValue]为null）
 */
suspend fun <T> LiveData<T>.await(): T? = if (version != LiveData.START_VERSION) {
    this.value
} else {
    nextValue()
}

suspend fun <T> LiveData<T>.nextValue(): T? {
    return suspendCancellableCoroutine { continuation ->
        val oldVersion = version

        @Suppress("kotlin:S6516")
        val observer = object : Observer<T?> {
            override fun onChanged(value: T?) {
                if (oldVersion != version) {
                    continuation.resume(value)
                    removeObserver(this)
                }
            }
        }
        observeForever(observer)
        continuation.invokeOnCancellation {
            removeObserver(observer)
        }
    }
}

inline fun <T> LiveData<T>.filter(
    crossinline filter: (T) -> Boolean
): LiveData<T> {
    val mediatorLiveData = MediatorLiveData<T>()
    mediatorLiveData.addSource(this) {
        if (filter(it)) {
            mediatorLiveData.value = it
        }
    }
    return mediatorLiveData
}

inline fun <T> LiveData<T>.observeWithOldValue(
    owner: LifecycleOwner,
    crossinline observer: (oldValue: T?, newValue: T) -> Unit
) {
    var oldValue: Pair<Int, T?> = versionKt to value
    observe(owner) {
        val old = oldValue
        if (old.first != versionKt) {
            oldValue = versionKt to it
        }
        observer(old.second, it)
    }
}

val <T> LiveData<T>.versionKt
    get() = version
