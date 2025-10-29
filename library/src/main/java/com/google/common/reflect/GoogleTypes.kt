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
interface GoogleTypes {

    fun newParameterizedTypeWithOwner(
        ownerType: Type?, rawType: Class<*>, vararg typeArguments: Type
    ): ParameterizedType

    fun subtypeOf(bound: Type): Type

    fun arrayOf(componentType: Type): Type

    object Gson : GoogleTypes {
        override fun newParameterizedTypeWithOwner(
            ownerType: Type?, rawType: Class<*>, vararg typeArguments: Type
        ): ParameterizedType = GsonTypes.newParameterizedTypeWithOwner(
            ownerType, rawType, *typeArguments
        )

        override fun subtypeOf(bound: Type): Type = GsonTypes.subtypeOf(
            bound
        )

        override fun arrayOf(componentType: Type): Type = GsonTypes.arrayOf(
            componentType
        )
    }

    object Guava : GoogleTypes {
        override fun newParameterizedTypeWithOwner(
            ownerType: Type?, rawType: Class<*>, vararg typeArguments: Type
        ): ParameterizedType = Types.newParameterizedTypeWithOwner(
            ownerType, rawType, *typeArguments
        )

        @SuppressLint("VisibleForTests")
        override fun subtypeOf(bound: Type): Type = Types.subtypeOf(
            bound
        )

        override fun arrayOf(componentType: Type): Type = Types.newArrayType(
            componentType
        )
    }

    object NotImpl : GoogleTypes {
        override fun newParameterizedTypeWithOwner(
            ownerType: Type?, rawType: Class<*>, vararg typeArguments: Type
        ): ParameterizedType {
            throw IllegalArgumentException("没有引入 Gson 或 Guava 依赖库")
        }

        override fun subtypeOf(bound: Type): Type {
            throw IllegalArgumentException("没有引入 Gson 或 Guava 依赖库")
        }

        override fun arrayOf(componentType: Type): Type {
            throw IllegalArgumentException("没有引入 Gson 或 Guava 依赖库")
        }
    }

    companion object : GoogleTypes {
        override fun newParameterizedTypeWithOwner(
            ownerType: Type?, rawType: Class<*>, vararg typeArguments: Type
        ): ParameterizedType = DependencyChecker.googleTypes.newParameterizedTypeWithOwner(
            ownerType, rawType, *typeArguments
        )

        override fun subtypeOf(bound: Type): Type =
            DependencyChecker.googleTypes.subtypeOf(bound)

        override fun arrayOf(componentType: Type): Type =
            DependencyChecker.googleTypes.arrayOf(componentType)
    }
}
