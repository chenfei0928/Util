package io.github.chenfei0928.concurrent.coroutines

import io.github.chenfei0928.app.ProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun ProgressDialog.launchWithShow(block: suspend CoroutineScope.() -> Unit) {
    coroutineScope.launch {
        try {
            block()
        } finally {
            dismiss()
        }
    }
    show()
}

suspend fun <T> ProgressDialog.showWithContext(
    context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T
): T {
    show()
    return try {
        withContext(
            context = coroutineScope.coroutineContext + context, block = block
        )
    } finally {
        dismiss()
    }
}
