package io.github.chenfei0928.base.mvp

import android.annotation.SuppressLint
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.github.chenfei0928.util.Log
import io.github.chenfei0928.util.SimpleLifecycleObserver
import io.github.chenfei0928.util.StackTraceLogUtil
import io.github.chenfei0928.lifecycle.onEvent

/**
 * Created by MrFeng on 18-7-4.
 */
open class BasePresenter<Contract : BaseContract>
@SuppressLint("CheckResult")
constructor(
        @JvmField protected val mLifecycleProvider: LifecycleOwner,
        @JvmField protected val mContract: Contract
) : LifecycleOwner {
    private val TAG = "KW_BasePresenter"
    protected var isAlive: Boolean = true

    init {
        checkPresenterContainer(mLifecycleProvider, mContract)
    }

    /**
     * 延时一个事件的订阅，直到fragment可见（View创建完毕或可见）
     */
    @JvmOverloads
    protected inline fun bindToCreatedEvent(onlyFirst: Boolean = false, crossinline action: () -> Unit) {
        val lifecycle = mLifecycleProvider.lifecycle
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            action()
            if (onlyFirst) {
                return
            }
        }
        lifecycle.addObserver(object : SimpleLifecycleObserver() {
            override fun onResume(owner: LifecycleOwner) {
                action()
                if (onlyFirst) {
                    lifecycle.removeObserver(this)
                }
            }
        })
    }

    @CallSuper
    protected open fun onDestroy() {
        isAlive = false
    }

    internal fun onContainerDestroy() {
        onDestroy()
    }

    override fun getLifecycle(): Lifecycle {
        return mLifecycleProvider.lifecycle
    }

    private fun checkPresenterContainer(lifecycleProvider: LifecycleOwner, contract: Contract) {
        when {
            lifecycleProvider is PresenterContainer ->
                lifecycleProvider.onBindPresenter(this)
            contract is PresenterContainer ->
                contract.onBindPresenter(this)
            else -> {
                Log.w(TAG, "checkPresenterContainer: ${this.javaClass.name}")
                StackTraceLogUtil.printStackTrace(TAG)
                lifecycleProvider.lifecycle.onEvent {
                    if (it == Lifecycle.Event.ON_DESTROY) {
                        this@BasePresenter.onDestroy()
                    }
                }
            }
        }
    }
}
