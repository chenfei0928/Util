package io.github.chenfei0928.view.asyncinflater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.UiThread
import io.github.chenfei0928.concurrent.ExecutorAndCallback

/**
 * @author chenf()
 * @date 2024-08-28 17:09
 */
interface IAsyncLayoutInflater {
    val inflater: LayoutInflater
    val executor: ExecutorAndCallback
    val executorOrScope: Any
        get() = executor

    @UiThread
    fun <VG : ViewGroup, R> inflate(
        onCreateView: (LayoutInflater, VG) -> R,
        parent: VG,
        callback: (R) -> Unit
    )

    @UiThread
    fun <VG : ViewGroup> inflate(
        @LayoutRes resId: Int,
        parent: VG?,
        callback: (View) -> Unit
    )
}
