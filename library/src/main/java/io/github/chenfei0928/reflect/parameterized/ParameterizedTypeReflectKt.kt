package io.github.chenfei0928.reflect.parameterized

import androidx.annotation.IntRange
import io.github.chenfei0928.reflect.arrayClass
import kotlin.reflect.KClass
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

/**
 * @author chenf()
 * @date 2024-07-08 10:47
 */
class ParameterizedTypeReflectKt<Parent : Any>(
    private val parentKClass: KClass<Parent>,
    private val finalChildKClass: KClass<out Parent>,
) {
    private val childClassNode: ParentParameterizedKtTypeNode =
        if (parentKClass == finalChildKClass) {
            ParentParameterizedKtTypeNode(finalChildKClass)
        } else {
            ParentParameterizedKtTypeNode.getParentTypeDefinedImplInChild(
                parentKClass, finalChildKClass
            ).first
        }

    fun <R> getParentParameterizedTypeDefinedImplInChild(
        @IntRange(from = 0) positionInParentParameter: Int
    ): Class<R> = if (parentKClass == finalChildKClass) {
        childClassNode.nodeKClass.typeParameters[positionInParentParameter].upperBounds.first().javaType as Class<R>
    } else getErasedTypeKClass(
        childClassNode, childClassNode.supertype?.arguments?.get(positionInParentParameter)!!
    )

    private fun <R> getErasedTypeKClass(
        currentNode: ParentParameterizedKtTypeNode,
        kTypeProjection: KTypeProjection?
    ): Class<R> {
        when (val kClassifier = kTypeProjection?.type?.classifier) {
            null -> {
                throw IllegalArgumentException(
                    "无法从指定类型中获取其泛型擦除后的类型." +
                            "\n父类：" + parentKClass +
                            "\n最终子类：" + finalChildKClass +
                            "\n当前子类：" + currentNode +
                            "\n当前子类实现的父类中的范型定义：" + kTypeProjection
                )
            }
            is KClass<*> -> {
                val jClass = kClassifier.java
                if (!jClass.isArray) {
                    // 当前子类直接指定了类型
                    return jClass as Class<R>
                } else if (jClass.componentType.isPrimitive) {
                    // 当前子类直接指定了原生类型数组类型
                    return jClass as Class<R>
                } else {
                    // 数组，但由子类负责进一步约束
                    val typeKClass =
                        getErasedTypeKClass<R>(currentNode, kTypeProjection.type!!.arguments[0])
                    return typeKClass.arrayClass() as Class<R>
                }
            }
            is KTypeParameter -> {
                // 当前子类进一步约束了范围，并由子类再次实现
                return findKTypeParameterClass(currentNode, kClassifier)
            }
            else -> {
                throw IllegalArgumentException(
                    "无法从指定类型中获取其泛型擦除后的类型." +
                            "\n父类：" + parentKClass +
                            "\n最终子类：" + finalChildKClass +
                            "\n当前子类：" + currentNode +
                            "\n当前子类实现的父类中的范型定义：" + kTypeProjection.javaClass + kTypeProjection
                )
            }
        }
    }

    private fun <R> findKTypeParameterClass(
        currentNode: ParentParameterizedKtTypeNode,
        kTypeParameter: KTypeParameter
    ): Class<R> {
        val typeParameters = currentNode.nodeKClass.typeParameters
        val index = typeParameters.indexOfFirst {
            it.name == kTypeParameter.name
        }
        val childNode = currentNode.childNode
        if (childNode == null) {
            return typeParameters[index].upperBounds.first().jvmErasure.java as Class<R>
        } else {
            val erasedTypeKClass = getErasedTypeKClass<Any>(
                childNode,
                childNode.supertype?.arguments?.get(index)!!
            )
            return erasedTypeKClass as Class<R>
        }
    }
}
