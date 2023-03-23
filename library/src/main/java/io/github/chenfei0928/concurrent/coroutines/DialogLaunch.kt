package io.github.chenfei0928.concurrent.coroutines

import android.app.Dialog
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

inline fun <D> D.launchWithShow(
    crossinline block: suspend CoroutineScope.(D) -> Unit
) where D : Dialog, D : LifecycleOwner {
    coroutineScope.launch {
        try {
            block(this@launchWithShow)
        } finally {
            dismiss()
        }
    }
    show()
}

suspend inline fun <D : Dialog, T> D.showWithContext(
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend CoroutineScope.(D) -> T
): T {
    show()
    return try {
        withContext(currentCoroutineContext() + context) {
            block(this@showWithContext)
        }
    } finally {
        dismiss()
    }
}
