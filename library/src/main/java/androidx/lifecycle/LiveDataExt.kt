/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-04-05 22:08
 */
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
        val observer = object : Observer<T?> {
            override fun onChanged(it: T?) {
                if (oldVersion != version) {
                    continuation.resume(it)
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

fun <T> LiveData<T>.filter(lifecycleOwner: LifecycleOwner, filter: (T) -> Boolean): LiveData<T> {
    val mutableLiveData = MutableLiveData<T>()
    this.observe(lifecycleOwner) {
        if (filter(it)) {
            mutableLiveData.value = it
        }
    }
    return mutableLiveData
}
