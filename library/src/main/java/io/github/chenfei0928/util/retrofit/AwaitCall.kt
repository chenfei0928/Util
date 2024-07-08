package io.github.chenfei0928.util.retrofit

import android.os.Looper
import io.github.chenfei0928.concurrent.ExecutorUtil
import io.github.chenfei0928.concurrent.lazy.lazyByAutoLoad
import io.github.chenfei0928.reflect.safeInvoke
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Method

internal class AwaitCall<T>(
    private val service: Any, private val method: Method, private val args: Array<Any?>?
) : Call<T> {
    private var canceled = false
    private val realCall = lazyByAutoLoad {
        method.safeInvoke(service, args) as Call<T>
    }

    override fun enqueue(callback: Callback<T>) {
        val looper = Looper.myLooper()
        if (looper !== Looper.getMainLooper() || realCall.isInitialized()) {
            // 其余线程的looper允许被退出looper，此处不能让其idle等待，需要同步调用
            realCall.value.realEnqueue(callback)
        } else {
            // 主线程
            ExecutorUtil.execute({
                realCall.value
            }, {
                it.realEnqueue(callback)
            })
        }
    }

    private fun Call<T>.realEnqueue(callback: Callback<T>) {
        if (canceled) {
            cancel()
        }
        enqueue(callback)
    }

    override fun isExecuted(): Boolean = realCall.isInitialized() && realCall.value.isExecuted

    override fun clone(): Call<T> = AwaitCall(service, method, args)

    override fun isCanceled(): Boolean {
        return if (canceled) {
            true
        } else if (!realCall.isInitialized()) {
            false
        } else {
            realCall.value.isCanceled
        }
    }

    override fun cancel() {
        canceled = true
        realCall.value.cancel()
    }

    override fun execute(): Response<T> {
        if (canceled) {
            realCall.value.cancel()
        }
        return realCall.value.execute()
    }

    override fun request(): Request = realCall.value.request()

    override fun timeout(): Timeout = realCall.value.timeout()
}
