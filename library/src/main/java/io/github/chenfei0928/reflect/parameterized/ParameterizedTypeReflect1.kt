package io.github.chenfei0928.reflect.parameterized

import androidx.annotation.IntRange
import com.google.common.reflect.GoogleTypes
import io.github.chenfei0928.collection.mapToArray
import io.github.chenfei0928.lang.arrayClass
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

/**
 * 使用Java反射获取子类在父类中实现的指定下标的范型类型，可以在不添加抽象方法时获取子类所实现的范型类型
 *
 * ```
 * Parent<R>
 * Child : Parent<R-Impl>
 * ```
 *
 * 在使用当前类时确认引入了[Gson](https://github.com/google/gson)依赖，
 * 当前类有使用 `com.google.gson.internal.$Gson$Types` 类
 *
 * 已知问题：
 * 1. 在获取Kotlin的类泛型定义时，List、Map的泛型子类继承后泛型约束会丢失，MutableList、MutableMap则没问题。
 *      详见 [io.github.chenfei0928.reflect.ParameterizedTypeReflect1Test.testParamKt] 的测试用例。
 *      Update: Kotlin 2.0 后实测已修复，
 *
 * @param Parent 父类类型
 * @param parentClass               父类类实例
 * @param finalChildClass           最终子类类实例
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-07 11:06
 */
class ParameterizedTypeReflect1<Parent>(
    private val parentClass: Class<Parent>,
    private val finalChildClass: Class<out Parent>,
) {
    private val childClassNode: ParentParameterizedTypeNode = if (parentClass == finalChildClass) {
        ParentParameterizedTypeNode(finalChildClass)
    } else {
        ParentParameterizedTypeNode.getParentTypeDefinedImplInChild(
            parentClass, finalChildClass
        ).first
    }
    private val childActualTypeArguments: Array<out Type> = if (parentClass == finalChildClass) {
        parentClass.typeParameters
    } else {
        val parameterizedType = childClassNode.genericSuperclass as ParameterizedType
        parameterizedType.actualTypeArguments
    }

    /**
     * 获取子类在父类中实现的指定下标的范型类型，可以在不添加抽象方法时获取子类所实现的范型类型
     *
     * @param positionInParentParameter 要获取的父类声明范型的指定下标
     * @param R 在子类实现的父类中指定声明的类型
     */
    @Suppress("UNCHECKED_CAST")
    fun <R> getParentParameterizedTypeDefinedImplInChild(
        @IntRange(from = 0) positionInParentParameter: Int
    ): Class<R> = getErasedTypeClass(
        childClassNode, childActualTypeArguments[positionInParentParameter]
    ) as Class<R>

    /**
     * 获取子类在父类中实现的指定下标的范型类型，可以在不添加抽象方法时获取子类所实现的范型类型
     *
     * @param positionInParentParameter 要获取的父类声明范型的指定下标
     */
    fun getType(
        @IntRange(from = 0) positionInParentParameter: Int
    ): Type = getType(childClassNode, childActualTypeArguments[positionInParentParameter])

    /**
     * 获取已擦除后的类型类
     *
     * @param currentNode 当前类节点
     * @param typeImplOnParent 当前类实现的父类中泛型定义（```Child : Parent<X>``` 中的X）
     */
    private fun getErasedTypeClass(
        currentNode: ParentParameterizedTypeNode, typeImplOnParent: Type
    ): Class<*> = when (typeImplOnParent) {
        is Class<*> -> {
            // 直接指定了范型元素类型
            typeImplOnParent
        }
        is TypeVariable<*> -> {
            // 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现，如：
            // Child<R> extends Parent<R>
            // Child<R extends XXX> extends Parent<R>
            // Child<R super XXX> extends Parent<R>
            val typeParameters = currentNode.typeParameters
            // 找到当前类该
            val indexOfCurrentNodeDefParameter = typeParameters.indexOfFirst {
                it.name == typeImplOnParent.name
            }
            currentNode.childNode?.let { childNode ->
                // 如果父类定义了泛型，但子类没有实现，此处不会返回ParameterizedType的实例（测试为父类类实例），需要去获取该子类父类的泛型范围
                val genericSuperclass = childNode.genericSuperclass as? ParameterizedType
                genericSuperclass?.let {
                    childNode to genericSuperclass.actualTypeArguments[indexOfCurrentNodeDefParameter]
                }
            }?.let { (childNode, typeImplOnParent) ->
                // 在子类中查找父类中泛型定义实现
                getErasedTypeClass(childNode, typeImplOnParent)
            } ?: run {
                // 获取该父类的泛型范围
                typeParameters[indexOfCurrentNodeDefParameter].bounds
                    .map { getErasedTypeClass(currentNode, it) }
                    .first()
            }
        }
        is GenericArrayType -> {
            // 如果是数组范型，还要查找子类中该范型数组元素的具体实现
            // Child extends Parent<XXX[]>
            // 将生成ChildR的数组
            getErasedTypeClass(
                currentNode, typeImplOnParent.genericComponentType
            ).arrayClass()
        }
        is ParameterizedType -> {
            // 子类的范型约束虽然是一个Interface或Class，但其仍有范型定义
            // Child<ChildR> extends Parent<List<ChildR>[]>
            // 将生成ChildR的数组
            // 由于有范型擦除机制，此处定义的 ChildR 在 List<ChildR>[] 中会被擦除，无法维持到运行时，只获取数组元素类型即可
            typeImplOnParent.rawType as Class<*>
        }
        is WildcardType -> {
            // 范型约束只约束了范围，而没有继续约束实现，通常不会被类实现，或许为字段或方法（？）
            typeImplOnParent.upperBounds
                .map { getErasedTypeClass(currentNode, it) }
                .first()
        }
        else -> throw IllegalArgumentException(
            "无法从指定类型中获取其泛型擦除后的类型." +
                    "\n父类：" + parentClass +
                    "\n最终子类：" + finalChildClass +
                    "\n当前子类：" + currentNode +
                    "\n当前子类实现的父类中的范型定义：" + typeImplOnParent.javaClass + typeImplOnParent
        )
    }

    /**
     * 获取已擦除后的类型类
     *
     * @param currentNode 当前类节点
     * @param typeImplOnParent 当前类实现的父类中泛型定义（```Child : Parent<X>``` 中的X）
     */
    private fun getType(
        currentNode: ParentParameterizedTypeNode, typeImplOnParent: Type
    ): Type = when (typeImplOnParent) {
        is Class<*> -> {
            // 直接指定了范型元素类型
            typeImplOnParent
        }
        is TypeVariable<*> -> {
            // 子类继续范型约束该类型，要继续向子类查找该范型在子类中的实现，如：
            // Child<R> extends Parent<R>
            // Child<R extends XXX> extends Parent<R>
            // Child<R super XXX> extends Parent<R>
            val typeParameters = currentNode.typeParameters
            // 找到当前类该
            val indexOfCurrentNodeDefParameter = typeParameters.indexOfFirst {
                it.name == typeImplOnParent.name
            }
            currentNode.childNode?.let { childNode ->
                // 如果父类定义了泛型，但子类没有实现，此处不会返回ParameterizedType的实例（测试为父类类实例），需要去获取该子类父类的泛型范围
                val genericSuperclass = childNode.genericSuperclass as? ParameterizedType
                genericSuperclass?.let {
                    childNode to genericSuperclass.actualTypeArguments[indexOfCurrentNodeDefParameter]
                }
            }?.let { (childNode, typeImplOnParent) ->
                // 在子类中查找父类中泛型定义实现
                getType(childNode, typeImplOnParent)
            } ?: run {
                // 获取该父类的泛型范围
                typeParameters[indexOfCurrentNodeDefParameter]
            }
        }
        is GenericArrayType -> {
            // 如果是数组范型，还要查找子类中该范型数组元素的具体实现
            // Child extends Parent<XXX[]>
            // 将生成ChildR的数组
            GoogleTypes.arrayOf(
                getType(
                    currentNode, typeImplOnParent.genericComponentType
                )
            )
        }
        is ParameterizedType -> {
            // 子类的范型约束虽然是一个Interface或Class，但其仍有范型定义
            // Child<ChildR> extends Parent<List<ChildR>[]>
            // 将生成ChildR的数组
            // 由于有范型擦除机制，此处定义的 ChildR 在 List<ChildR>[] 中会被擦除，无法维持到运行时，只获取数组元素类型即可
            @Suppress("SpreadOperator")
            GoogleTypes.newParameterizedTypeWithOwner(
                typeImplOnParent.ownerType,
                typeImplOnParent.rawType as Class<*>,
                *typeImplOnParent.actualTypeArguments.mapToArray {
                    getType(currentNode, it)
                }
            )
        }
        is WildcardType -> {
            // 范型约束只约束了范围，而没有继续约束实现，通常不会被类实现，或许为字段或方法（？）
            typeImplOnParent
        }
        else -> throw IllegalArgumentException(
            "无法从指定类型中获取其泛型擦除后的类型." +
                    "\n父类：" + parentClass +
                    "\n最终子类：" + finalChildClass +
                    "\n当前子类：" + currentNode +
                    "\n当前子类实现的父类中的范型定义：" + typeImplOnParent.javaClass + typeImplOnParent
        )
    }
}
