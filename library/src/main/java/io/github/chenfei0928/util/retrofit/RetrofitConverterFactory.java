package io.github.chenfei0928.util.retrofit;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 用于将字符串请求直接转换为请求体
 * Created by MrFeng on 2017/5/8.
 */
public class RetrofitConverterFactory extends Converter.Factory {
    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            @NotNull Type type,
            @NotNull Annotation[] parameterAnnotations,
            @NotNull Annotation[] methodAnnotations,
            @NotNull Retrofit retrofit
    ) {
        if (String.class == type) {
            return (Converter<String, RequestBody>) RetrofitUtil::wrap;
        }
        return null;
    }
}
