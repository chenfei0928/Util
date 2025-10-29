/**
 * 提供获取子类中的泛型定义的类对象信息
 *
 * 涉及反射，会消耗较多时间，耗时排名从低到高：
 * 1. [getParentParameterizedTypeClassDefinedImplInChild] useKtReflect=true 有缓存（甚至会达到0.4ms）
 * 2. [getParentParameterizedTypeClassDefinedImplInChild] useKtReflect=false，
 *  [getParentParameterizedTypeBoundsContractDefinedImplInChild] （无缓存约30ms，有缓存约2-5ms）
 * 3. [getParentParameterizedTypeClassDefinedImplInChild] useKtReflect=false 无缓存（可能会消耗1000ms）
 *
 * 如果启用R8FullMode后由于其压缩类继承栈等导致调用该方法时出现了NPE等情况，
 * 可以对继承路径上各个类（包含Parent至进行其泛型约束的Child）添加 [Keep] 、 [KeepAllowObfuscation]
 * 或 [KeepAllowOptimizationShrinkingObfuscation] 注解解决
 * （Gson 的 [TypeToken] 也在混淆规则中添加了对其所有子类的keep）
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-02-20 18:01
 */
package io.github.chenfei0928.reflect.parameterized

import androidx.annotation.IntRange
import androidx.annotation.Keep
import com.google.common.reflect.GoogleTypes
import com.google.gson.reflect.TypeToken
import io.github.chenfei0928.annotation.KeepAllowObfuscation
import io.github.chenfei0928.annotation.KeepAllowOptimizationShrinkingObfuscation
import java.lang.reflect.Type

inline fun <reified Parent : Any, R> Parent.getParentParameterizedTypeBoundsContractDefinedImplInChild(
    @IntRange(from = 0) positionInParentParameter: Int
): TypeBoundsContract<R> = ParameterizedTypeReflect0.getParentParameterizedTypeDefinedImplInChild(
    Parent::class.java, this::class.java, positionInParentParameter
)

/**
 * 使用Java或Kotlin反射获取子类在父类中实现的指定下标的范型类型，可以在不添加抽象方法时获取子类所实现的范型类型
 *
 * 如果 [useKtReflect] 为`true`时需要正确实现了 [GoogleTypes]
 *
 * 通常情况下（在没有缓存时），Java反射在性能方面比Kotlin好
 *
 * @param Parent 父类类型
 */
inline fun <reified Parent : Any, R> Parent.getParentParameterizedTypeClassDefinedImplInChild(
    @IntRange(from = 0) positionInParentParameter: Int, useKtReflect: Boolean = false
): Class<R> = if (useKtReflect) {
    ParameterizedTypeReflect2(
        Parent::class, this::class
    ).getParentParameterizedTypeDefinedImplInChild(positionInParentParameter)
} else {
    ParameterizedTypeReflect1(
        Parent::class.java, this::class.java
    ).getParentParameterizedTypeDefinedImplInChild(positionInParentParameter)
}

/**
 * 使用Java反射获取子类在父类中实现的指定下标的范型类型，可以在不添加抽象方法时获取子类所实现的范型类型
 *
 * 在使用当前类时需要正确实现了 [GoogleTypes]
 *
 * @param Parent 父类类型
 */
inline fun <reified Parent : Any> Parent.getParentParameterizedTypeDefinedImplInChild(
    @IntRange(from = 0) positionInParentParameter: Int
): Type = ParameterizedTypeReflect1(
    Parent::class.java, this::class.java
).getType(positionInParentParameter)
