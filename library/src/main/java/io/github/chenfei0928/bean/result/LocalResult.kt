package io.github.chenfei0928.bean.result

import android.content.Intent
import com.alibaba.android.arouter.facade.Postcard
import io.github.chenfei0928.retrofitUtil.LHNetApiUtil

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-10-15 18:22
 */
sealed class LocalResult<T> {

    class Error<T>(
        message: String? = null, val exception: Throwable
    ) : LocalResult<T>() {

        constructor(exception: Throwable) : this(null, exception)

        val message: String? = message ?: exception.message
    }

    class AIntent<T>(
        val intent: Intent
    ) : LocalResult<T>()

    class ARouter<T>(
        val postcard: Postcard
    ) : LocalResult<T>()

    class Success<T>(
        val content: T
    ) : LocalResult<T>()

    companion object {

        fun <T> fromNetResult(result: NetResult<T>): LocalResult<T> = try {
            val content = result.content
            if (result.requestSuccess() && content != null) {
                Success(content)
            } else {
                LHNetApiUtil.checkResultOrThrow(result)
                Error(NullPointerException("未返回有效结果"))
            }
        } catch (e: Throwable) {
            Error(e)
        }
    }
}
