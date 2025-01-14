package io.github.chenfei0928.reflect.parameterized

import androidx.annotation.IntRange
import io.github.chenfei0928.lang.arrayClass
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.jvm.jvmErasure

/**
 * 用Kotlin反射获取类型信息
 * 获取子类在父类中实现的指定下标的范型类型，可以在不添加抽象方法时获取子类所实现的范型类型
 *
 * ```
 * Parent<R>
 * Child : Parent<R-Impl>
 * ```
 *
 * @param Parent 父类类型
 * @param parentKClass              父类类实例
 * @param finalChildKClass          最终子类类实例
 *
 * @author chenf()
 * @date 2024-07-08 10:47
 */
@Suppress("UNCHECKED_CAST", "kotlin:S6531")
class ParameterizedTypeReflect2<Parent : Any>(
    private val parentKClass: KClass<Parent>,
    private val finalChildKClass: KClass<out Parent>,
) {
    /**
     * 获取子类在父类中实现的指定下标的范型类型，可以在不添加抽象方法时获取子类所实现的范型类型
     *
     * @param positionInParentParameter 要获取的父类声明范型的指定下标
     * @param R 在子类实现的父类中指定声明的类型
     */
    fun <R> getParentParameterizedTypeDefinedImplInChild(
        @IntRange(from = 0) positionInParentParameter: Int
    ): Class<R> = if (parentKClass == finalChildKClass) {
        ParentParameterizedKtTypeNode(finalChildKClass)
            .nodeKClass
            .typeParameters[positionInParentParameter]
            .jvmErasureJavaClass()
    } else {
        val childClassNode = ParentParameterizedKtTypeNode.getParentTypeDefinedImplInChild(
            parentKClass, finalChildKClass
        ).first
        getErasedTypeClass(
            childClassNode, childClassNode.getSupertypeArgumentType(positionInParentParameter)
        )
    }

    /**
     * 获取已擦除后的类型类
     *
     * @param R 在子类实现的父类中指定声明的类型
     * @param currentNode 当前类节点
     * @param kType 当前类实现的父类中泛型定义（```Child : Parent<X>``` 中的X）
     * @return 已擦除后的类型类
     */
    private fun <R> getErasedTypeClass(
        currentNode: ParentParameterizedKtTypeNode,
        kType: KType?
    ): Class<R> = when (val kClassifier = kType?.classifier) {
        null -> {
            // Kotlin不支持对其进行描述的类型，如复合类型
            throw IllegalArgumentException(
                "无法从指定类型中获取其泛型擦除后的类型." +
                        "\n父类：" + parentKClass +
                        "\n最终子类：" + finalChildKClass +
                        "\n当前子类：" + currentNode +
                        "\n当前子类实现的父类中的范型定义：" + kType
            )
        }
        is KClass<*> -> {
            val jClass = kClassifier.java
            /* return */ if (!jClass.isArray) {
                // 当前子类直接指定了一个非数组类型
                jClass as Class<R>
            } else if (jClass.componentType.isPrimitive) {
                // 当前子类直接指定了原生类型数组类型
                jClass as Class<R>
            } else {
                // 数组，但由子类负责进一步约束
                val typeKClass = getErasedTypeClass<R>(
                    currentNode, kType.arguments[0].type
                )
                typeKClass.arrayClass() as Class<R>
            }
        }
        is KTypeParameter -> {
            // 当前子类进一步约束了范围，并由子类再次实现
            // 查找当前泛型约束在类头定义的下标顺序
            val typeParameters = currentNode.nodeKClass.typeParameters
            val index = typeParameters.indexOfFirst {
                it.name == kClassifier.name
            }
            val childNode = currentNode.childNode
            /* return */ if (childNode == null) {
                // 没有子节点，直接获取当前节点的泛型约束范围
                typeParameters[index].jvmErasureJavaClass()
            } else {
                // 有子节点，进一步获取子节点的泛型约束
                val erasedTypeKClass = getErasedTypeClass<Any>(
                    childNode, childNode.getSupertypeArgumentType(index)
                )
                erasedTypeKClass as Class<R>
            }
        }
        else -> {
            throw IllegalArgumentException(
                "无法从指定类型中获取其泛型擦除后的类型." +
                        "\n父类：" + parentKClass +
                        "\n最终子类：" + finalChildKClass +
                        "\n当前子类：" + currentNode +
                        "\n当前子类实现的父类中的范型定义：" + kType.javaClass + kType
            )
        }
    }

    private fun <R> KTypeParameter.jvmErasureJavaClass(): Class<R> = upperBounds.run {
        firstNotNullOf { it.jvmErasure.java } as? Class<R>
            ?: firstOrNull()?.jvmErasure?.java as? Class<R>
            ?: Any::class.java as Class<R>
    }
}
