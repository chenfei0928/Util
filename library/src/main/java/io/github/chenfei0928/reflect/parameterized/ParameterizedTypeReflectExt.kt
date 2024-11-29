package io.github.chenfei0928.reflect.parameterized

import androidx.annotation.IntRange
import java.lang.reflect.Type

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-02-20 18:01
 */
@Deprecated("使用 getParentParameterizedTypeClassDefinedImplInChild 获取 class 而非 Type")
inline fun <reified Parent : Any, R> Parent.getParentParameterizedTypeBoundsContractDefinedImplInChild(
    @IntRange(from = 0) positionInParentParameter: Int
): TypeBoundsContract<R> = ParameterizedTypeReflect0.getParentParameterizedTypeDefinedImplInChild(
    Parent::class.java, this::class.java, positionInParentParameter
)

inline fun <reified Parent : Any, R> Parent.getParentParameterizedTypeClassDefinedImplInChild(
    @IntRange(from = 0) positionInParentParameter: Int
): Class<R> = ParameterizedTypeReflect2(
    Parent::class, this::class
).getParentParameterizedTypeDefinedImplInChild(positionInParentParameter)

@Deprecated("使用 getParentParameterizedTypeClassDefinedImplInChild 获取 class 而非 Type")
inline fun <reified Parent : Any> Parent.getParentParameterizedTypeDefinedImplInChild(
    @IntRange(from = 0) positionInParentParameter: Int
): Type = ParameterizedTypeReflect1(
    Parent::class.java, this::class.java
).getType(positionInParentParameter)
