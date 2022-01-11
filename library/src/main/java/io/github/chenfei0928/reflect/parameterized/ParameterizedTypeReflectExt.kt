package io.github.chenfei0928.reflect.parameterized

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-02-20 18:01
 */
inline fun <reified Parent : Any, R> Parent.getParentParameterizedTypeDefinedImplInChild(
    positionInParentParameter: Int
): TypeBoundsContract<R> {
    return ParameterizedTypeReflect0.getParentParameterizedTypeDefinedImplInChild(
        Parent::class.java, this::class.java, positionInParentParameter
    )
}

inline fun <reified Parent : Any, R> Parent.getParentParameterizedTypeClassDefinedImplInChild(
    positionInParentParameter: Int
): Class<R> {
    return ParameterizedTypeReflect0.getParentParameterizedTypeDefinedImplInChild1(
        Parent::class.java, this::class.java, positionInParentParameter
    )
}
