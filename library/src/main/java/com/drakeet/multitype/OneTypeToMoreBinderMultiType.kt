/**
 * 实现一实体类型对应多binder注册的扩展函数
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2019-11-19 18:02
 */
package com.drakeet.multitype

import io.github.chenfei0928.util.Log

private const val TAG = "KW_MultiType"

/**
 * 注册一个类型的一type对多binder的映射关系，并且使用map字典[viewTypeRecord]来保存实例与binder类的映射。
 *
 * 考虑到binder类定义时会使用抽象类隔离ui逻辑与点击事件，[viewTypeRecord]的binder类定义到[binders]的实现
 * 不允许出现任何一定义对多实现的关系。
 * 即在[viewTypeRecord]中所出现的各个值（类对象）之间不允许出现任何继承关系，也不允许出现一个类对象在[binders]
 * 中有多个实现提供，否则会在使用该类对象在[binders]中查找该类对象实现时出现找到的并不是其最终实现类的情况。
 *
 * [viewTypeRecord]的map通常会使用依赖于对象hash的map实现，此时要确保[T]的类没有重写[Any.hashCode]方法，
 * 以避免列表出现重复内容数据时hash冲突（重复），而覆盖了之前实例的binder映射关系。
 * 并要求在某项被记录其binder之后，不允许修改其实例属性，否则可能会导致其hash变化，影响查找其值。
 * 或可以直接使用[androidx.collection.SystemIdentityArrayMap]实现的map，
 * 其会使用JVM提供的[Object.hashCode]默认实现，可避免由于对象内容相同导致的hash碰撞。
 *
 * @param clazz binder要渲染的bean的类型
 * @param binders 实现该类型布局绑定器的实例
 * @param viewTypeRecord 要显示的实例与该实例所使用的布局绑定器映射关系。
 */
@Suppress("unused")
fun <T> MultiTypeAdapter.registerWithTypeRecordClassMap(
    clazz: Class<T>,
    binders: Array<ItemViewDelegate<T, *>>,
    viewTypeRecord: Map<T, Class<out ItemViewDelegate<T, *>>>
) {
    registerWithTypeRecord(clazz, binders) { item ->
        viewTypeRecord[item]
    }
}

/**
 * 注册一个类型的一type对多binder的映射关系，并且使用map字典[viewTypeRecord]来保存实例与binder类的映射。
 *
 * 考虑到binder类定义时会使用抽象类隔离ui逻辑与点击事件，[viewTypeRecord]的binder类定义到[binders]的实现
 * 不允许出现任何一定义对多实现的关系。
 * 即在[viewTypeRecord]中所出现的各个值（类对象）之间不允许出现任何继承关系，也不允许出现一个类对象在[binders]
 * 中有多个实现提供，否则会在使用该类对象在[binders]中查找该类对象实现时出现找到的并不是其最终实现类的情况。
 *
 * [viewTypeRecord]的map通常会使用依赖于对象hash的map实现，此时要确保[T]的类没有重写[Any.hashCode]方法，
 * 以避免列表出现重复内容数据时hash冲突（重复），而覆盖了之前实例的binder映射关系。
 * 并要求在某项被记录其binder之后，不允许修改其实例属性，否则可能会导致其hash变化，影响查找其值。
 * 或可以直接使用[androidx.collection.SystemIdentityArrayMap]实现的map，
 * 其会使用JVM提供的[Object.hashCode]默认实现，可避免由于对象内容相同导致的hash碰撞。
 *
 * @param binders 实现该类型布局绑定器的实例
 * @param viewTypeRecord 要显示的实例与该实例所使用的布局绑定器映射关系。
 */
fun <T> MultiTypeAdapter.registerWithTypeRecorderMap(
    clazz: Class<T>,
    binders: Array<ItemViewDelegate<T, *>>,
    viewTypeRecord: Map<in T, ViewTypeProvider>
) {
    registerWithTypeRecord(clazz, binders) { item ->
        viewTypeRecord[item]?.binderClazz
    }
}

/**
 * 注册一个类型的一type对多binder的映射关系，并且通过回调选择其binder类映射。
 *
 * 考虑到binder类定义时会使用抽象类隔离ui逻辑与点击事件，[viewTypeRecorder]的binder类定义到[binders]的实现
 * 不允许出现任何一定义对多实现的关系。
 * 即在[viewTypeRecorder]中所出现的各个值（类对象）之间不允许出现任何继承关系，也不允许出现一个类对象在[binders]
 * 中有多个实现提供，否则会在使用该类对象在[binders]中查找该类对象实现时出现找到的并不是其最终实现类的情况。
 *
 * @param binders 实现该类型布局绑定器的实例
 * @param viewTypeRecorder 提供要显示的实例与该实例所使用的布局绑定器映射关系。
 */
private inline fun <T> MultiTypeAdapter.registerWithTypeRecord(
    clazz: Class<T>,
    binders: Array<ItemViewDelegate<T, *>>,
    crossinline viewTypeRecorder: (T) -> Class<out ItemViewDelegate<*, *>>?
) {
    registerWithLinker(clazz, binders) linker@{ localBinders, _, item ->
        // 获取该item所使用的binder类
        val binderClazz = viewTypeRecorder(item)
        if (binderClazz == null) {
            Log.w(TAG, "registerWithTypeRecordClassMap: item not found binder class.\n$item")
            return@linker 0
        }
        // 查找第几个是该binder类的实例
        val index = localBinders.indexOfFirst {
            binderClazz.isInstance(it)
        }
        return@linker if (index != -1) {
            index
        } else {
            Log.w(
                TAG,
                "registerWithTypeRecordClassMap: item binder class not found binder instance.\n$item"
            )
            0
        }
    }
}

/**
 * 注册一个类型的一type对多binder的映射关系，并使用linker来进行分配管理
 */
inline fun <reified T> MultiTypeAdapter.registerWithLinker(
    binders: Array<ItemViewDelegate<T, *>>, crossinline linker: BindersLinker<T>
) {
    registerWithLinker(T::class.java, binders, linker)
}

/**
 * 注册一个类型的一type对多binder的映射关系，并使用linker来进行分配管理
 */
inline fun <T> MultiTypeAdapter.registerWithLinker(
    clazz: Class<T>, binders: Array<ItemViewDelegate<T, *>>, crossinline linker: BindersLinker<T>
) {
    register(clazz)
        .to(*binders)
        .withLinker(object : Linker<T> {
            override fun index(position: Int, item: T): Int {
                return linker(binders, position, item)
            }
        })
}

/**
 * 传入binders，并返回指定的item所使用的binder在binders中对应的下标
 * [KotlinClassLinker]
 */
typealias BindersLinker<T> = (binders: Array<ItemViewDelegate<T, *>>, position: Int, item: T) -> Int
