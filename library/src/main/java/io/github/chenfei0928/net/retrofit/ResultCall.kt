package io.github.chenfei0928.net.retrofit

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-27 14:54
 */
class ResultCall<T>(
    private val call: Call<T>,
    private val before: Boolean = true,
    private val successCallback: (response: Response<T>) -> Unit
) : CallDelegate<T>(call) {

    override fun enqueue(callback: Callback<T>) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (before) {
                    successCallback(response)
                    callback.onResponse(this@ResultCall, response)
                } else {
                    callback.onResponse(this@ResultCall, response)
                    successCallback(response)
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onFailure(this@ResultCall, t)
            }
        })
    }

    override fun execute(): Response<T> {
        val execute = call.execute()
        successCallback(execute)
        return execute
    }
}
