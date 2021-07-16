package com.chenfei.retrofitUtil

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class CallDelegate<T>(
    call: Call<T>
) : Call<T> by call {
    abstract override fun enqueue(callback: Callback<T>)

    abstract override fun execute(): Response<T>
}

class FinishCallbackCall<T>(
    private val call: Call<T>,
    private val requestFinish: () -> Unit
) : CallDelegate<T>(call) {
    override fun enqueue(callback: Callback<T>) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                try {
                    callback.onResponse(this@FinishCallbackCall, response)
                } finally {
                    requestFinish()
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                try {
                    callback.onFailure(this@FinishCallbackCall, t)
                } finally {
                    requestFinish()
                }
            }
        })
    }

    override fun cancel() {
        call.cancel()
        requestFinish()
    }

    override fun execute(): Response<T> {
        val execute = call.execute()
        requestFinish()
        return execute
    }
}
