package io.github.chenfei0928.preference.base

import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.preference.LocalStoragePreferenceDataStore
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/**
 * 用于 [LocalStoragePreferenceDataStore] 允许直接修改原对象返回持久化的持久化器
 * @author chenf()
 * @date 2024-12-31 11:21
 */
interface MutableFieldAccessor<T> : DataCopyClassFieldAccessor<T> {

    //<editor-fold desc="KMutableProperty的访问方法" defaultstatus="collapsed">
    /**
     * 注册一个可直接读写字段的字段
     *
     * @param V 值类型
     * @param property [T]的某个字段
     * @return 该字段描述说明
     */
    fun <V> property(
        property: KMutableProperty1<T, V>,
        vType: PreferenceType,
    ): FieldAccessor.Field<T, V>

    /**
     * 注册一个可直接读写字段的子字段，先获取[T]的某个结构体字段[T1]，再从[T1]中获取[V]
     *
     * @param T1 中间值类型
     * @param V 值类型
     * @param property0 [T]的某个字段
     * @return 该字段描述说明
     */
    fun <T1, V> property(
        property0: KMutableProperty1<T, T1>,
        property1: KMutableProperty1<T1, V>,
        vType: PreferenceType,
    ): FieldAccessor.Field<T, V>

    /**
     * 注册一个可直接读写字段的子字段，先获取[T]的某个结构体字段[T1]，再从[T1]中获取[T2]，再从[T2]中获取[V]
     *
     * @param T1 中间值类型
     * @param T2 中间值类型
     * @param V 值类型
     * @param property0 [T]的某个字段
     * @return 该字段描述说明
     */
    fun <T1, T2, V> property(
        property0: KMutableProperty1<T, T1>,
        property1: KMutableProperty1<T1, T2>,
        property2: KMutableProperty1<T2, V>,
        vType: PreferenceType,
    ): FieldAccessor.Field<T, V>
    //</editor-fold>

    open class Impl<T : Any>(
        private val redirectToMutableField: Boolean,
        readCache: Boolean,
    ) : DataCopyClassFieldAccessor.Impl<T>(readCache), MutableFieldAccessor<T> {
        override fun <T, V> field(
            tCopyFunc: KFunction<T>,
            tProperty: KProperty1<T, V>,
            vType: PreferenceType?,
        ): FieldAccessor.Field<T, V> {
            return if (redirectToMutableField && tProperty is KMutableProperty1) {
                field<T, V>(tProperty, vType)
            } else {
                super.field(tCopyFunc, tProperty, vType)
            }
        }

        //<editor-fold desc="KMutableProperty的访问方法" defaultstatus="collapsed">
        protected open fun <T, V> field(
            property: KMutableProperty1<T, V>,
            vType: PreferenceType? = null
        ): FieldAccessor.Field<T, V> = KMutablePropertyField(
            property, vType
        )

        /**
         * 注册一个可直接读写字段的字段
         *
         * @param V 值类型
         * @param property [T]的某个字段
         * @return 该字段描述说明
         */
        override fun <V> property(
            property: KMutableProperty1<T, V>,
            vType: PreferenceType,
        ): FieldAccessor.Field<T, V> = field(
            property, vType
        ).let(this::property)

        /**
         * 注册一个可直接读写字段的子字段，先获取[T]的某个结构体字段[T1]，再从[T1]中获取[V]
         *
         * @param T1 中间值类型
         * @param V 值类型
         * @param property0 [T]的某个字段
         * @return 该字段描述说明
         */
        override fun <T1, V> property(
            property0: KMutableProperty1<T, T1>,
            property1: KMutableProperty1<T1, V>,
            vType: PreferenceType,
        ): FieldAccessor.Field<T, V> = property(
            field(property0),
            field(property1, vType),
        )

        /**
         * 注册一个可直接读写字段的子字段，先获取[T]的某个结构体字段[T1]，再从[T1]中获取[T2]，再从[T2]中获取[V]
         *
         * @param T1 中间值类型
         * @param T2 中间值类型
         * @param V 值类型
         * @param property0 [T]的某个字段
         * @return 该字段描述说明
         */
        override fun <T1, T2, V> property(
            property0: KMutableProperty1<T, T1>,
            property1: KMutableProperty1<T1, T2>,
            property2: KMutableProperty1<T2, V>,
            vType: PreferenceType,
        ): FieldAccessor.Field<T, V> = property(
            field(property0),
            field(property1),
            field(property2, vType),
        )

        /**
         * 使用可写属性来进行读写的字段
         *
         * @param T 宿主类类型
         * @param V 字段类型
         * @property property [T]的某个字段
         */
        private class KMutablePropertyField<T, V>(
            private val property: KMutableProperty1<T, V>,
            vType: PreferenceType? = null,
        ) : FieldAccessor.Field<T, V> {
            override val pdsKey: String = property.name
            override val vType: PreferenceType by lazy(LazyThreadSafetyMode.NONE) {
                vType ?: PreferenceType.forType(property.returnType)
            }

            override fun get(data: T): V = property.get(data)
            override fun set(data: T, value: V): T {
                property.set(data, value)
                return data
            }

            override fun toString(): String = "KMutablePropertyField($pdsKey:$vType)"
        }
        //</editor-fold>
    }

    companion object {
        //<editor-fold desc="KMutableProperty的访问方法" defaultstatus="collapsed">
        inline fun <reified T : Any, reified V : Any> MutableFieldAccessor<T>.property(
            tProperty: KMutableProperty1<T, V>,
        ): FieldAccessor.Field<T, V> = property(
            tProperty, PreferenceType.forType<V>()
        )

        inline fun <reified T : Any, reified T1 : Any, reified V> MutableFieldAccessor<T>.property(
            tProperty: KMutableProperty1<T, T1>,
            t1Property: KMutableProperty1<T1, V>,
        ): FieldAccessor.Field<T, V> = property(
            tProperty, t1Property, PreferenceType.forType<V>()
        )

        inline fun <reified T : Any, reified T1 : Any, reified T2 : Any, reified V>
                MutableFieldAccessor<T>.property(
            tProperty: KMutableProperty1<T, T1>,
            t1Property: KMutableProperty1<T1, T2>,
            t2Property: KMutableProperty1<T2, V>,
        ): FieldAccessor.Field<T, V> = property(
            tProperty, t1Property, t2Property, PreferenceType.forType<V>()
        )
        //</editor-fold>
    }
}
