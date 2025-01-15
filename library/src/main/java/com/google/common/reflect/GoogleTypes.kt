package com.google.common.reflect

import android.annotation.SuppressLint
import io.github.chenfei0928.util.DependencyChecker
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import com.google.gson.internal.`$Gson$Types` as GsonTypes

/**
 * @author chenf()
 * @date 2025-01-14 11:28
 */
object GoogleTypes {
    fun newParameterizedTypeWithOwner(
        ownerType: Type?, rawType: Type, vararg typeArguments: Type
    ): ParameterizedType = when {
        DependencyChecker.GSON() -> GsonTypes.newParameterizedTypeWithOwner(
            ownerType, rawType, *typeArguments
        )
        DependencyChecker.GUAVA() -> Types.newParameterizedTypeWithOwner(
            ownerType, rawType as Class<*>, *typeArguments
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
