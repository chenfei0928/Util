package io.github.chenfei0928.net.retrofit

import okhttp3.RequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * 用于将字符串请求直接转换为请求体
 *
 * Created by MrFeng on 2017/5/8.
 */
class RetrofitConverterFactory : Converter.Factory() {

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody?>? {
        if (String::class.java == type) {
            return Converter<String, RequestBody?> { obj: String ->
                RetrofitUtil.wrap(obj)
            }
        }
        return null
    }
}
