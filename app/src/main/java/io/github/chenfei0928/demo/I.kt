package io.github.chenfei0928.demo

import io.github.chenfei0928.annotation.KeepAllowOptimizationShrinkingObfuscation
import io.github.chenfei0928.annotation.WithChildInObfuscation
import io.github.chenfei0928.reflect.parameterized.TypeBoundsContract
import io.github.chenfei0928.reflect.parameterized.getParentParameterizedTypeBoundsContractDefinedImplInChild
import io.github.chenfei0928.reflect.parameterized.getParentParameterizedTypeClassDefinedImplInChild

/**
 * @author chenf()
 * @date 2024-12-16 16:17
 */
@WithChildInObfuscation
@KeepAllowOptimizationShrinkingObfuscation
abstract class I<E : Any> {
    abstract val useKtReflect: Boolean
    val typeBundle: TypeBoundsContract<E> =
        getParentParameterizedTypeBoundsContractDefinedImplInChild<I<*>, E>(0)
    val eClass: Class<E> =
        getParentParameterizedTypeClassDefinedImplInChild<I<*>, E>(0, useKtReflect)

    open class I1<E, T : List<E>>(
        override val useKtReflect: Boolean
    ) : I<T>() {
        open class IM<E, T : MutableList<E>>(
            useKtReflect: Boolean
        ) : I1<E, T>(useKtReflect)

        class IArrayList(
            useKtReflect: Boolean
        ) : IM<Any, ArrayList<Any>>(useKtReflect)
    }

    companion object {
        const val TAG = "I"
        var testMember = 1
        private val privateTestMember = 2
    }
}
