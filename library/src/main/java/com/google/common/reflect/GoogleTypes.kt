package com.google.common.reflect

import android.annotation.SuppressLint
import com.google.gson.internal.GsonTypes
import io.github.chenfei0928.util.DependencyChecker
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 使用 Google 开源库中的 [Type] 处理类来提供泛型构建
 * 依赖了 [Gson](https://github.com/google/gson) 或 [Guava](https://github.com/google/guava) 库作为内部实现。
 *
 * @author chenf()
 * @date 2025-01-14 11:28
 */
object GoogleTypes {
    fun newParameterizedTypeWithOwner(
        ownerType: Type?, rawType: Class<*>, vararg typeArguments: Type
    ): ParameterizedType = when {
        DependencyChecker.gson -> GsonTypes.newParameterizedTypeWithOwner(
            ownerType, rawType, *typeArguments
        )
        DependencyChecker.guava -> Types.newParameterizedTypeWithOwner(
            ownerType, rawType, *typeArguments
        )
        else -> throw throwException()
    }

    @SuppressLint("VisibleForTests")
    fun subtypeOf(bound: Type): Type = when {
        DependencyChecker.gson -> GsonTypes.subtypeOf(
            bound
        )
        DependencyChecker.guava -> Types.subtypeOf(
            bound
        )
        else -> throw throwException()
    }

    fun arrayOf(componentType: Type) = when {
        DependencyChecker.gson -> GsonTypes.arrayOf(
            componentType
        )
        DependencyChecker.guava -> Types.newArrayType(
            componentType
        )
        else -> throw throwException()
    }

    private fun throwException() = IllegalArgumentException("没有引入 Gson 或 Guava 依赖库")
}
