package io.github.chenfei0928.util

import io.github.chenfei0928.collection.SystemIdentityMutableMap
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 对结构体进行属性扩展的委托实现类，通过弱引用来保存映射
 * 可以对对象的属性进行扩展追加，提供或默认通过Map映射实体类和扩展的属性值的映射
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-09-06 10:01
 */
abstract class BeanExtValDelegate<Bean, R>(
    private val map: MutableMap<Bean, R> = SystemIdentityMutableMap(HashMap())
) : ReadOnlyProperty<Bean, R> {

    override fun getValue(thisRef: Bean, property: KProperty<*>): R {
        return map.getOrPut(thisRef) { create(thisRef) }
    }

    abstract fun create(thisRef: Bean): R
}
