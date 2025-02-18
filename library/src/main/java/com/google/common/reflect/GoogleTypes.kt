package com.google.common.reflect

import android.annotation.SuppressLint
import io.github.chenfei0928.util.DependencyChecker
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import com.google.gson.internal.`$Gson$Types` as GsonTypes

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
        DependencyChecker.GSON() -> GsonTypes.newParameterizedTypeWithOwner(
            ownerType, rawType, *typeArguments
        )
        DependencyChecker.GUAVA() -> Types.newParameterizedTypeWithOwner(
            ownerType, rawType, *typeArguments
        )
        else -> throw IllegalArgumentException("没有引入依赖库")
    }

    @SuppressLint("VisibleForTests")
    fun subtypeOf(bound: Type): Type = when {
        DependencyChecker.GSON() -> GsonTypes.subtypeOf(
            bound
        )
        DependencyChecker.GUAVA() -> Types.subtypeOf(
            bound
        )
        else -> throw IllegalArgumentException("没有引入依赖库")
    }

    fun arrayOf(componentType: Type) = when {
        DependencyChecker.GSON() -> GsonTypes.arrayOf(
            componentType
        )
        DependencyChecker.GUAVA() -> Types.newArrayType(
            componentType
        )
        else -> throw IllegalArgumentException("没有引入依赖库")
    }
}
