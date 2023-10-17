package io.github.chenfei0928.reflect.parameterized

import java.lang.reflect.Type

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-02-20 18:01
 */
inline fun <reified Parent : Any, R> Parent.getParentParameterizedTypeBoundsContractDefinedImplInChild(
    positionInParentParameter: Int
): TypeBoundsContract<R> {
    return ParameterizedTypeReflect0.getParentParameterizedTypeDefinedImplInChild(
        Parent::class.java, this::class.java, positionInParentParameter
    )
}

inline fun <reified Parent : Any, R> Parent.getParentParameterizedTypeClassDefinedImplInChild(
    positionInParentParameter: Int
): Class<R> {
    return ParameterizedTypeReflect.getParentParameterizedTypeClassDefinedImplInChild(
        Parent::class.java, this::class.java, positionInParentParameter
    )
}

inline fun <reified Parent : Any> Parent.getParentParameterizedTypeDefinedImplInChild(
    positionInParentParameter: Int
): Type {
    return ParameterizedTypeReflect.getParentParameterizedTypeDefinedImplInChild(
        Parent::class.java, this::class.java, positionInParentParameter
    )
}
