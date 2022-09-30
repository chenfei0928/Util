/**
 * 修复[androidx.activity.result.registerForActivityResult]中
 * inputIntent使用直接传入的方式在Activity实例初始化阶段，可能会部分参数无法获取到而导致crash的问题
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-06-01 17:13
 */
package io.github.chenfei0928.app.result

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat

/**
 * A version of [ActivityResultCaller.registerForActivityResult]
 * that additionally takes an input right away, producing a launcher that doesn't take any
 * additional input when called.
 *
 * @see ActivityResultCaller.registerForActivityResult
 */
inline fun <I, O> ActivityResultCaller.registerForActivityResult(
    contract: ActivityResultContract<I, O>,
    crossinline input: () -> I,
    registry: ActivityResultRegistry? = null,
    callback: ActivityResultCallback<O>
): ActivityResultLauncher<Unit> {
    val resultLauncher = if (registry != null) {
        registerForActivityResult(contract, registry, callback)
    } else {
        registerForActivityResult(contract, callback)
    }
    return object : ActivityResultCallerLauncher<I, O>(resultLauncher, contract) {
        override fun input(): I {
            return input()
        }
    }
}

abstract class ActivityResultCallerLauncher<I, O>(
    private val launcher: ActivityResultLauncher<I>,
    private val callerContract: ActivityResultContract<I, O>,
) : ActivityResultLauncher<Unit>() {
    private val resultContract: ActivityResultContract<Unit, O> by lazy {
        object : ActivityResultContract<Unit, O>() {
            override fun createIntent(context: Context, input: Unit): Intent {
                return callerContract.createIntent(context, input())
            }

            override fun parseResult(resultCode: Int, intent: Intent?): O {
                return callerContract.parseResult(resultCode, intent)
            }
        }
    }

    override fun launch(void: Unit?, options: ActivityOptionsCompat?) {
        launcher.launch(input(), options)
    }

    override fun unregister() {
        launcher.unregister()
    }

    override fun getContract(): ActivityResultContract<Unit, O> {
        return resultContract
    }

    protected abstract fun input(): I
}
