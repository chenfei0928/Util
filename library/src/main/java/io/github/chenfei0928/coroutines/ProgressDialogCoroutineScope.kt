package io.github.chenfei0928.coroutines

import android.app.Activity
import android.content.DialogInterface
import androidx.fragment.app.Fragment
import io.github.chenfei0928.app.ProgressDialog
import io.github.chenfei0928.base.app.BaseApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Closeable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 生命周期宿主的协程领域缓存
 */
private val progressDialogCoroutineScopeCache: MutableMap<ProgressDialog, ProgressDialogCoroutineScope> =
    mutableMapOf()

val ProgressDialog.coroutineScope: CoroutineScope
    get() = run {
        // 宿主存活时，创建或从缓存中获取一个与该宿主生命周期绑定的协程实例
        progressDialogCoroutineScopeCache.getOrPut(this) {
            val coroutineScope = ProgressDialogCoroutineScope(this)
            this@coroutineScope.internalDismissListener = coroutineScope
            this@coroutineScope.internalShowListener = coroutineScope
            coroutineScope
        }
    }

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

/**
 * 与生命周期绑定的协程上下文，带有异常处理
 * [博文](https://juejin.im/post/5cfb38f96fb9a07eeb139a00)
 */
private class ProgressDialogCoroutineScope(
    private val host: ProgressDialog
) : JobCoroutineScope(MainScope.coroutineContext), Closeable, DialogInterface.OnDismissListener,
    DialogInterface.OnShowListener {
    override val coroutineContext: CoroutineContext
        get() = super.coroutineContext + // 生命周期的协程
                androidContextElement

    override fun onShow(dialog: DialogInterface?) {
        job.start()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        // 关闭页面后，结束所有协程任务
        close()
    }

    private val androidContextElement: CoroutineAndroidContext = run {
        when (val context = host.context) {
            is Fragment -> {
                CoroutineAndroidContextImpl(context.activity ?: context.requireContext(), context)
            }
            is Activity -> {
                CoroutineAndroidContextImpl(context, null)
            }
            else -> {
                CoroutineAndroidContextImpl(BaseApplication.getInstance(), null)
            }
        }
    }

    /**
     * 关闭该协程，将自身从缓存中移除，并不再监听宿主的生命周期
     */
    override fun close() {
        job.cancel()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            progressDialogCoroutineScopeCache.remove(host, this)
        } else {
            progressDialogCoroutineScopeCache.remove(host)
        }
        host.setOnDismissListener(null)
        host.setOnShowListener(null)
    }
}
