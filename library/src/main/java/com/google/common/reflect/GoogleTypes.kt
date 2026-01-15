package com.google.common.reflect

import android.annotation.SuppressLint
import com.google.gson.internal.GsonTypes
import io.github.chenfei0928.util.DependencyChecker
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

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

    fun subtypeOf(bound: Type): WildcardType

    fun arrayOf(componentType: Type): Type

    //<editor-fold desc="Gson的实现" defaultstatus="collapsed">
    object Gson : GoogleTypes {
        override fun newParameterizedTypeWithOwner(
            ownerType: Type?, rawType: Class<*>, vararg typeArguments: Type
        ): ParameterizedType = GsonTypes.newParameterizedTypeWithOwner(
            ownerType, rawType, *typeArguments
        )

        override fun subtypeOf(bound: Type): WildcardType = GsonTypes.subtypeOf(
            bound
        )

        override fun arrayOf(componentType: Type): Type = GsonTypes.arrayOf(
            componentType
        )
    }
    //</editor-fold>

    //<editor-fold desc="Guava的实现" defaultstatus="collapsed">
    object Guava : GoogleTypes {
        override fun newParameterizedTypeWithOwner(
            ownerType: Type?, rawType: Class<*>, vararg typeArguments: Type
        ): ParameterizedType = Types.newParameterizedTypeWithOwner(
            ownerType, rawType, *typeArguments
        )

        @SuppressLint("VisibleForTests")
        override fun subtypeOf(bound: Type): WildcardType = Types.subtypeOf(
            bound
        )

        override fun arrayOf(componentType: Type): Type = Types.newArrayType(
            componentType
        )
    }
    //</editor-fold>

    //<editor-fold desc="未实现" defaultstatus="collapsed">
    object NotImpl : GoogleTypes {
        override fun newParameterizedTypeWithOwner(
            ownerType: Type?, rawType: Class<*>, vararg typeArguments: Type
        ): ParameterizedType = throwNotImpl()

        override fun subtypeOf(bound: Type): WildcardType = throwNotImpl()
        override fun arrayOf(componentType: Type): Type = throwNotImpl()
        private fun throwNotImpl(): Nothing =
            throw IllegalArgumentException("没有引入 Gson 或 Guava 依赖库")
    }
    //</editor-fold>

    companion object : GoogleTypes {
        override fun newParameterizedTypeWithOwner(
            ownerType: Type?, rawType: Class<*>, vararg typeArguments: Type
        ): ParameterizedType = DependencyChecker.googleTypes.newParameterizedTypeWithOwner(
            ownerType, rawType, *typeArguments
        )

        override fun subtypeOf(bound: Type): WildcardType =
            DependencyChecker.googleTypes.subtypeOf(bound)

        override fun arrayOf(componentType: Type): Type =
            DependencyChecker.googleTypes.arrayOf(componentType)
    }
}
