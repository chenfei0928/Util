package io.github.chenfei0928.concurrent.coroutines

import io.github.chenfei0928.app.ProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

inline fun ProgressDialog.launchWithShow(
    crossinline block: suspend CoroutineScope.(ProgressDialog) -> Unit
) {
    coroutineScope.launch {
        try {
            block(this@launchWithShow)
        } finally {
            dismiss()
        }
    }
    show()
}

suspend inline fun <T> ProgressDialog.showWithContext(
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline block: suspend CoroutineScope.(ProgressDialog) -> T
): T {
    show()
    return try {
        withContext(coroutineScope.coroutineContext + context) {
            block(this@showWithContext)
        }
    } finally {
        dismiss()
    }
}
