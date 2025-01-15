package io.github.chenfei0928.reflect

import android.util.Log
import io.github.chenfei0928.lang.javaPrimitiveType
import io.github.chenfei0928.reflect.parameterized.ParameterizedTypeReflect1
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.util.LinkedList
import java.util.Queue

fun Class<*>.isSubclassOf(base: Class<*>): Boolean =
    base.isAssignableFrom(this)

fun Type.isSubtypeOf(base: Type): Boolean =
    SubtypeChecker().isSubtypeOf(this, base)

private class SubtypeChecker(
    private val sameBoxedAndPrimitiveType: Boolean = true,
    private val indexOfTypeVariable: Int = 0,
) {
    // 替换为 Host.typeParameters[which] = WhoClass 如果Host which WhoClass 一致则不判断 WhoClass
    private val parentParameterChain: Queue<ParametersCheckRecord> = LinkedList()

    /**
     * @param sameBoxedAndPrimitiveType 将JVM装箱类型与原始类型认为是同一个类型
     * @author chenf()
     * @date 2025-01-14 16:43
     */
    @Suppress("CyclomaticComplexMethod")
    fun isSubtypeOf(child: Type, base: Type): Boolean = if (base == Any::class.java) {
        true
    } else when (base) {
        is Class<*> -> isSubtypeOfClass(child, base)
        is ParameterizedType -> isSubtypeOfParameterizedType(child, base)
        is GenericArrayType -> isSubTypeOfGenericArrayType(child, base)
        is WildcardType -> isSubTypeOfWildcardType(child, base)
        is TypeVariable<*> -> isSubTypeOfTypeVariable(child, base)
        else -> @Suppress("UseRequire") throw IllegalArgumentException(
            "Not support type: ${child.javaClass.name} $child and base ${base.javaClass.name} $base"
        )
    }

    @Suppress("CyclomaticComplexMethod")
    private fun isSubtypeOfClass(
        child: Type, base: Class<*>
    ): Boolean = when (child) {
        // 两个都是具体类，检查装箱信息或继承信息
        is Class<*> -> if (sameBoxedAndPrimitiveType && (child.isPrimitive || base.isPrimitive)) {
            child.javaPrimitiveType() == base.javaPrimitiveType()
        } else if (!base.isAssignableFrom(child)) {
            false
        } else isSubtypeOfParameterizedTypeActualTypeArgumentsOrClassTypeParameters(
            child, base, base.typeParameters
        )
        is ParameterizedType -> if (!isSubtypeOf(child.rawType, base)) {
            false
        } else {
            val baseTypeParameters = base.typeParameters
            if (baseTypeParameters.all { it == Any::class.java }) {
                true
            } else {
                // todo 中间可能还有继承关系、其 rawType 依然可能有泛型，并交由使用处填充定义
                //  检查 base.typeParameters 的泛型
                Log.d(TAG, run {
                    "isSubtypeOfClass: type ParameterizedType $child and base ParameterizedType $base"
                })
                true
            }
        }
        is GenericArrayType -> base.isArray && isSubtypeOf(
            child.genericComponentType, base.componentType!!
        )
        is WildcardType -> child.upperBounds.all { isSubtypeOf(it, base) }
                && isSubtypeOfUpperBounds(base, child.lowerBounds)
        is TypeVariable<*> -> isSubtypeOf(child.bounds[indexOfTypeVariable], base)
        else -> @Suppress("UseRequire") throw IllegalArgumentException(
            "Not support type: ${child.javaClass.name} $child and base $base"
        )
    }

    private fun isSubtypeOfParameterizedType(
        child: Type, base: ParameterizedType
    ): Boolean = when (child) {
        // 父类是带泛型的，子类不带泛型，如base是 Enum<*>
        is Class<*> -> if (!isSubtypeOf(child, base.rawType)) {
            false
        } else isSubtypeOfParameterizedTypeActualTypeArgumentsOrClassTypeParameters(
            child, base.rawType as Class<*>, base.actualTypeArguments
        )
        is ParameterizedType -> {
            val childOwnerType = child.ownerType
            val baseOwnerType = base.ownerType
            if (baseOwnerType != null && childOwnerType == null) {
                // 非静态内部类，如果基类有宿主类，而子类没有宿主类，不允许的情况
                false
            } else if (baseOwnerType != null && !isSubtypeOf(childOwnerType!!, baseOwnerType)) {
                // 如果有宿主类，子类的宿主类类型也必须是基类的子类或一致
                false
            } else if (!isSubtypeOf(child.rawType, base.rawType)) {
                // 校验类是否是子类
                false
                // 校验基类中各个参数当前子类是否均已经有实现或更细致的约束
            } else base.actualTypeArguments.all { baseType ->
                child.actualTypeArguments.any { isSubtypeOf(it, baseType) }
            }
        }
        is GenericArrayType -> false
        is WildcardType -> {
            child.upperBounds.all { isSubtypeOf(base, it) }
                    && child.lowerBounds.all { isSubtypeOf(it, base) }
        }
        is TypeVariable<*> -> isSubtypeOfParameterizedType(
            child.bounds[indexOfTypeVariable], base
        )
        else -> @Suppress("UseRequire") throw IllegalArgumentException(
            "Not support type: ${child.javaClass.name} $child and base $base"
        )
    }

    /**
     * @param sameBoxedAndPrimitiveType 将JVM装箱类型与原始类型认为是同一个类型
     */
    private fun isSubTypeOfGenericArrayType(
        child: Type, base: GenericArrayType
    ): Boolean = when (child) {
        is Class<*> -> child.isArray && isSubtypeOf(
            child.componentType!!, base.genericComponentType
        )
        is ParameterizedType -> false
        is GenericArrayType -> isSubtypeOf(
            child.genericComponentType, base.genericComponentType
        )
        is WildcardType -> false
        is TypeVariable<*> -> false
        else -> @Suppress("UseRequire") throw IllegalArgumentException(
            "Not support type: ${child.javaClass.name} $child and base $base"
        )
    }

    private fun isSubTypeOfWildcardType(
        child: Type, base: WildcardType
    ): Boolean = if (
        base.upperBounds.all { it == Any::class.java }
        && base.lowerBounds.isNullOrEmpty()
    ) {
        true
    } else when (child) {
        is Class<*> ->
            isSubtypeOfUpperBounds(child, base.upperBounds)
                    && base.lowerBounds.all { isSubtypeOf(it, child) }
        is ParameterizedType ->
            isSubtypeOfUpperBounds(child.rawType, base.upperBounds)
                    && base.lowerBounds.all { isSubtypeOf(it, child.rawType) }
        is GenericArrayType -> false
        is WildcardType -> {
            // 两者都是范围约束泛型，检查参数上下限
            base.upperBounds.all { baseType ->
                child.upperBounds.any { isSubtypeOf(it, baseType) }
            } && base.lowerBounds.any { baseType ->
                child.lowerBounds.any { isSubtypeOf(baseType, it) }
            }
        }
        is TypeVariable<*> -> child.bounds[indexOfTypeVariable].let { childType ->
            base.upperBounds.all { baseType -> isSubtypeOf(childType, baseType) }
                    && base.lowerBounds.any { baseType -> isSubtypeOf(baseType, childType) }
        }
        else -> @Suppress("UseRequire") throw IllegalArgumentException(
            "Not support type: ${child.javaClass.name} $child and base $base"
        )
    }

    private fun isSubTypeOfTypeVariable(
        child: Type, base: TypeVariable<*>
    ): Boolean = if (base.bounds.all { it == Any::class.java }) {
        true
    } else when (child) {
        is Class<*> -> isSubtypeOfUpperBounds(child, base.bounds)
        is ParameterizedType -> isSubtypeOfUpperBounds(child.rawType, base.bounds)
        is GenericArrayType -> false
        is WildcardType -> base.bounds.all { baseType ->
            child.upperBounds.any { isSubtypeOf(it, baseType) }
        }
        is TypeVariable<*> -> base.bounds.all { baseType ->
            child.bounds.any { isSubtypeOf(it, baseType) }
        }
        else -> @Suppress("UseRequire") throw IllegalArgumentException(
            "Not support type: ${child.javaClass.name} $child and base $base"
        )
    }

    private fun isSubtypeOfUpperBounds(
        child: Type, upperBounds: Array<Type>
    ): Boolean = if (upperBounds.all { it == Any::class.java }) {
        true
    } else when (child) {
        is Class<*> -> upperBounds.all { upperBound ->
            isSubtypeOf(child, upperBound)
        }
        is ParameterizedType -> upperBounds.all { upperBound ->
            isSubtypeOf(child.rawType, upperBound)
        }
        is GenericArrayType -> false
        is WildcardType -> upperBounds.all { upperBound ->
            child.upperBounds.any { isSubtypeOf(it, upperBound) }
        }
        is TypeVariable<*> -> upperBounds.all { upperBound ->
            isSubtypeOf(child.bounds[indexOfTypeVariable], upperBound)
        }
        else -> @Suppress("UseRequire") throw IllegalArgumentException(
            "Not support type: ${child.javaClass.name} $child and upperBounds ${upperBounds.contentToString()}"
        )
    }

    private fun isSubtypeOfParameterizedTypeActualTypeArgumentsOrClassTypeParameters(
        child: Class<*>, base: Class<*>, baseActualTypeArguments: Array<out Type>
    ): Boolean = if (baseActualTypeArguments.all { it == Any::class.java }) {
        true
    } else {
        @Suppress("UNCHECKED_CAST")
        val typeChecker = ParameterizedTypeReflect1(base as Class<Any>, child)
        baseActualTypeArguments.forEachIndexed { index, type ->
            if (type == Any::class.java) {
                return@forEachIndexed
            }
            val typeRecord = ParametersCheckRecord(base, index, type)
            // 检查该泛型是否是自引用或循环引用，避免重复检查出现StackOverflow
            if (typeRecord in parentParameterChain) {
                return true
            }
            parentParameterChain.offer(typeRecord)
            val typeIndex = typeChecker.getType(index)
            typeRecord
            if (!isSubtypeOf(typeIndex, type)) {
                parentParameterChain.remove(typeRecord)
                return false
            }
            parentParameterChain.remove(typeRecord)
        }
        true
    }

    /**
     * 宿主类、指定泛型下标、改下标的类信息
     *
     * 用于记录如 [Enum] 和 [com.google.protobuf.GeneratedMessageLite] 之类存在泛型自引用和循环引用的情况
     */
    private data class ParametersCheckRecord(
        private val hostClass: Class<*>,
        private val whichIndexOfTypeParameter: Int,
        private val whoType: Type,
    )

    companion object {
        private const val TAG = "SubtypeChecker"
    }
}
