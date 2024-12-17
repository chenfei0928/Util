package io.github.chenfei0928.preference.datastore

import androidx.collection.ArrayMap
import io.github.chenfei0928.util.MapCache
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.functions
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.valueParameters

/**
 * Kotlin data class字段访问器存储与获取
 *
 * @author chenf()
 * @date 2024-10-12 18:40
 */
interface FieldAccessorHelper<T> : FieldAccessor<T> {

    //<editor-fold desc="data class copy方法" defaultstatus="collapsed">
    /**
     * 注册一个 data class 的 copy 方法
     *
     * @param T0 data class类型类
     * @param clazz data class类型类
     * @param copyFunc 其的copy函数
     */
    fun <T0 : Any> cacheCopyFunc(clazz: KClass<T0>, copyFunc: KFunction<T0>)

    /**
     * 获取该data class的copy函数
     */
    val <T : Any> KClass<T>.copyFunc: KFunction<T>
    //</editor-fold>

    //<editor-fold desc="给data class的字段拷贝访问方法" defaultstatus="collapsed">
    /**
     * 注册一个data class的字段
     *
     * @param V 值类型
     * @param tCopyFunc [T] 类型的copy函数
     * @param tProperty [T]的某个字段
     * @return 该字段描述说明
     */
    fun <V> properties(
        tCopyFunc: KFunction<T>,
        tProperty: KProperty1<T, V>,
    ): FieldAccessor.Field<T, V>

    /**
     * 注册一个data class的字段的子字段，先获取[T]的某个结构体字段[T1]，再从[T1]中获取[V]
     *
     * @param T1 中间值类型
     * @param V 值类型
     * @param tCopyFunc [T] 类型的copy函数
     * @param tProperty [T]的某个字段
     * @return 该字段描述说明
     */
    fun <T1 : Any, V> properties(
        tCopyFunc: KFunction<T>,
        tProperty: KProperty1<T, T1>,
        t1CopyFunc: KFunction<T1>,
        t1Property: KProperty1<T1, V>,
    ): FieldAccessor.Field<T, V>

    /**
     * 注册一个data class的字段的子字段，先获取[T]的某个结构体字段[T1]，再从[T1]中获取[T2]，再从[T2]中获取[V]
     *
     * @param T1 中间值类型
     * @param T2 中间值类型
     * @param V 值类型
     * @param tCopyFunc [T] 类型的copy函数
     * @param tProperty [T]的某个字段
     * @return 该字段描述说明
     */
    @Suppress("LongParameterList")
    fun <T1 : Any, T2 : Any, V> properties(
        tCopyFunc: KFunction<T>,
        tProperty: KProperty1<T, T1>,
        t1CopyFunc: KFunction<T1>,
        t1Property: KProperty1<T1, T2>,
        t2CopyFunc: KFunction<T2>,
        t2Property: KProperty1<T2, V>,
    ): FieldAccessor.Field<T, V>
    //</editor-fold>

    //<editor-fold desc="KMutableProperty的访问方法" defaultstatus="collapsed">
    /**
     * 注册一个可直接读写字段的字段
     *
     * @param V 值类型
     * @param property [T]的某个字段
     * @return 该字段描述说明
     */
    fun <V> properties(
        property: KMutableProperty1<T, V>
    ): FieldAccessor.Field<T, V>

    /**
     * 注册一个可直接读写字段的子字段，先获取[T]的某个结构体字段[T1]，再从[T1]中获取[V]
     *
     * @param T1 中间值类型
     * @param V 值类型
     * @param property0 [T]的某个字段
     * @return 该字段描述说明
     */
    fun <T1, V> properties(
        property0: KMutableProperty1<T, T1>,
        property1: KMutableProperty1<T1, V>,
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
    fun <T1, T2, V> properties(
        property0: KMutableProperty1<T, T1>,
        property1: KMutableProperty1<T1, T2>,
        property2: KMutableProperty1<T2, V>,
    ): FieldAccessor.Field<T, V>
    //</editor-fold>

    //<editor-fold desc="对其他的访问" defaultstatus="collapsed">
    fun <T1, V> properties(
        outerField: FieldAccessor.Field<T, T1>,
        innerField: FieldAccessor.Field<T1, V>,
    ): FieldAccessor.Field<T, V>
    //</editor-fold>

    open class Impl<T : Any> : FieldAccessor.Impl<T>(), FieldAccessorHelper<T> {
        //<editor-fold desc="data class copy方法" defaultstatus="collapsed">
        private val dataClassCopyFuncCache =
            MapCache<KClass<*>, KFunction<*>>(ArrayMap()) { kClass ->
                require(kClass.isData) { "only data class can use copyFunc: $this" }
                val parameters = kClass.primaryConstructor?.valueParameters
                kClass.functions.find {
                    !it.isSuspend
                            && it.name == "copy"
                            && it.returnType.classifier == kClass
                            && !it.returnType.isMarkedNullable
                            && it.valueParameters == parameters
                }!!
            }

        /**
         * 注册一个 data class 的 copy 方法
         *
         * @param T0 data class类型类
         * @param clazz data class类型类
         * @param copyFunc 其的copy函数
         */
        override fun <T0 : Any> cacheCopyFunc(clazz: KClass<T0>, copyFunc: KFunction<T0>) {
            dataClassCopyFuncCache[clazz] = copyFunc
        }

        /**
         * 获取该data class的copy函数
         */
        @Suppress("kotlin:S6531", "UNCHECKED_CAST")
        override val <T : Any> KClass<T>.copyFunc: KFunction<T>
            get() = dataClassCopyFuncCache[this] as KFunction<T>
        //</editor-fold>

        //<editor-fold desc="给data class的字段拷贝访问方法" defaultstatus="collapsed">
        /**
         * 注册一个data class的字段
         *
         * @param V 值类型
         * @param tCopyFunc [T] 类型的copy函数
         * @param tProperty [T]的某个字段
         * @return 该字段描述说明
         */
        override fun <V> properties(
            tCopyFunc: KFunction<T>,
            tProperty: KProperty1<T, V>,
        ): FieldAccessor.Field<T, V> = KPropertyDataCopyField(
            tCopyFunc, tProperty
        ).let(::property)

        /**
         * 注册一个data class的字段的子字段，先获取[T]的某个结构体字段[T1]，再从[T1]中获取[V]
         *
         * @param T1 中间值类型
         * @param V 值类型
         * @param tCopyFunc [T] 类型的copy函数
         * @param tProperty [T]的某个字段
         * @return 该字段描述说明
         */
        override fun <T1 : Any, V> properties(
            tCopyFunc: KFunction<T>,
            tProperty: KProperty1<T, T1>,
            t1CopyFunc: KFunction<T1>,
            t1Property: KProperty1<T1, V>,
        ): FieldAccessor.Field<T, V> = KPropertyDataCopyWrapper(
            KPropertyDataCopyField(
                tCopyFunc, tProperty
            ), t1CopyFunc, t1Property
        ).let(::property)

        /**
         * 注册一个data class的字段的子字段，先获取[T]的某个结构体字段[T1]，再从[T1]中获取[T2]，再从[T2]中获取[V]
         *
         * @param T1 中间值类型
         * @param T2 中间值类型
         * @param V 值类型
         * @param tCopyFunc [T] 类型的copy函数
         * @param tProperty [T]的某个字段
         * @return 该字段描述说明
         */
        @Suppress("LongParameterList")
        override fun <T1 : Any, T2 : Any, V> properties(
            tCopyFunc: KFunction<T>,
            tProperty: KProperty1<T, T1>,
            t1CopyFunc: KFunction<T1>,
            t1Property: KProperty1<T1, T2>,
            t2CopyFunc: KFunction<T2>,
            t2Property: KProperty1<T2, V>,
        ): FieldAccessor.Field<T, V> = KPropertyDataCopyWrapper(
            KPropertyDataCopyWrapper(
                KPropertyDataCopyField(
                    tCopyFunc, tProperty
                ), t1CopyFunc, t1Property
            ), t2CopyFunc, t2Property
        ).let(::property)

        /**
         * 使用只读属性与该data class类的 copy 方法来进行读写的字段
         *
         * @param T 宿主类类型
         * @param V 字段类型
         * @property copyFunc [T] 类的copy函数
         * @property property [T]的某个字段
         */
        class KPropertyDataCopyField<T : Any, V>(
            private val copyFunc: KFunction<T>,
            private val property: KProperty1<T, V>
        ) : FieldAccessor.Field<T, V> {
            override val name: String = property.name
            private val instanceParameter: KParameter = copyFunc.instanceParameter!!
            private val kParameter: KParameter = copyFunc.parameters.find {
                it.name == property.name
            }!!

            override fun get(data: T): V {
                return property.get(data)
            }

            override fun set(data: T, value: V): T {
                return copyFunc.callBy(
                    mapOf(
                        instanceParameter to data,
                        kParameter to value,
                    )
                )
            }
        }

        /**
         * 使用只读属性与该data class类的 copy 方法来进行读写的字段
         *
         * @param T 宿主类类型
         * @param T1 中间宿主类类型
         * @param V 字段类型
         * @property outerField 外部字段到中间宿主类型字段的访问方法
         * @property innerCopyFunc [T1] 类的copy函数
         * @property innerProperty [T1]的某个字段
         */
        class KPropertyDataCopyWrapper<T : Any, T1 : Any, V>(
            private val outerField: FieldAccessor.Field<T, T1>,
            private val innerCopyFunc: KFunction<T1>,
            private val innerProperty: KProperty1<T1, V>
        ) : FieldAccessor.Field<T, V> {
            override val name: String = outerField.name + "_" + innerProperty.name
            private val instanceParameter: KParameter = innerCopyFunc.instanceParameter!!
            private val kParameter: KParameter = innerCopyFunc.parameters.find {
                it.name == innerProperty.name
            }!!

            override fun get(data: T): V {
                return innerProperty.get(outerField.get(data))
            }

            override fun set(data: T, value: V): T {
                val copied = innerCopyFunc.callBy(
                    mapOf(
                        instanceParameter to outerField.get(data),
                        kParameter to value
                    )
                )
                return outerField.set(data, copied)
            }
        }
        //</editor-fold>

        //<editor-fold desc="KMutableProperty的访问方法" defaultstatus="collapsed">
        /**
         * 注册一个可直接读写字段的字段
         *
         * @param V 值类型
         * @param property [T]的某个字段
         * @return 该字段描述说明
         */
        override fun <V> properties(
            property: KMutableProperty1<T, V>
        ): FieldAccessor.Field<T, V> = KMutablePropertyField(
            property
        ).let(this::property)


        /**
         * 注册一个可直接读写字段的子字段，先获取[T]的某个结构体字段[T1]，再从[T1]中获取[V]
         *
         * @param T1 中间值类型
         * @param V 值类型
         * @param property0 [T]的某个字段
         * @return 该字段描述说明
         */
        override fun <T1, V> properties(
            property0: KMutableProperty1<T, T1>,
            property1: KMutableProperty1<T1, V>,
        ): FieldAccessor.Field<T, V> = KMutablePropertyWrapper(
            KMutablePropertyField(
                property0
            ), property1
        ).let(::property)


        /**
         * 注册一个可直接读写字段的子字段，先获取[T]的某个结构体字段[T1]，再从[T1]中获取[T2]，再从[T2]中获取[V]
         *
         * @param T1 中间值类型
         * @param T2 中间值类型
         * @param V 值类型
         * @param property0 [T]的某个字段
         * @return 该字段描述说明
         */
        override fun <T1, T2, V> properties(
            property0: KMutableProperty1<T, T1>,
            property1: KMutableProperty1<T1, T2>,
            property2: KMutableProperty1<T2, V>,
        ): FieldAccessor.Field<T, V> = KMutablePropertyWrapper(
            KMutablePropertyWrapper(
                KMutablePropertyField(
                    property0
                ), property1
            ), property2
        ).let(::property)

        /**
         * 使用可写属性来进行读写的字段
         *
         * @param T 宿主类类型
         * @param V 字段类型
         * @property property [T]的某个字段
         */
        private class KMutablePropertyField<T, V>(
            private val property: KMutableProperty1<T, V>
        ) : FieldAccessor.Field<T, V> {
            override val name: String = property.name

            override fun get(data: T): V {
                return property.get(data)
            }

            override fun set(data: T, value: V): T {
                property.set(data, value)
                return data
            }
        }

        /**
         * 使用可写属性来进行读写的字段
         *
         * @param T 宿主类类型
         * @param T1 中间宿主类类型
         * @param V 字段类型
         * @property outerField 外部字段到中间宿主类型字段的访问方法
         * @property innerProperty [T1]的某个字段
         */
        private class KMutablePropertyWrapper<T, T1, V>(
            private val outerField: FieldAccessor.Field<T, T1>,
            private val innerProperty: KMutableProperty1<T1, V>,
        ) : FieldAccessor.Field<T, V> {
            override val name: String = outerField.name + "_" + innerProperty.name

            override fun get(data: T): V {
                return innerProperty.get(outerField.get(data))
            }

            override fun set(data: T, value: V): T {
                val t1 = outerField.get(data)
                innerProperty.set(t1, value)
                // 内部实例自身属性变更，没有产生新实例，不需要重新set外部实例
                return data
            }
        }
        //</editor-fold>

        //<editor-fold desc="对其他的访问" defaultstatus="collapsed">
        override fun <T1, V> properties(
            outerField: FieldAccessor.Field<T, T1>,
            innerField: FieldAccessor.Field<T1, V>,
        ): FieldAccessor.Field<T, V> = FieldWrapper(outerField, innerField).let(::property)

        /**
         * 使用只读属性与该data class类的 copy 方法来进行读写的字段
         *
         * @param T 宿主类类型
         * @param T1 中间宿主类类型
         * @param V 字段类型
         * @property outerField 外部字段到中间宿主类型字段的访问方法
         * @property innerField 中间宿主的子字段的访问方法
         */
        private class FieldWrapper<T, T1, V>(
            private val outerField: FieldAccessor.Field<T, T1>,
            private val innerField: FieldAccessor.Field<T1, V>,
        ) : FieldAccessor.Field<T, V> {
            override val name: String = outerField.name + "_" + innerField.name

            override fun get(data: T): V {
                return innerField.get(outerField.get(data))
            }

            override fun set(data: T, value: V): T {
                val t1 = outerField.get(data)
                return outerField.set(data, innerField.set(t1, value))
            }
        }
        //</editor-fold>
    }

    companion object {
        //<editor-fold desc="data class copy方法" defaultstatus="collapsed">
        inline fun <reified T0 : Any> FieldAccessorHelper<T0>.cacheCopyFunc(copyFunc: KFunction<T0>) {
            cacheCopyFunc(T0::class, copyFunc)
        }
        //</editor-fold>

        //<editor-fold desc="给data class的字段拷贝访问方法" defaultstatus="collapsed">
        inline fun <reified T : Any, V : Any> FieldAccessorHelper<T>.properties(
            tProperty: KProperty1<T, V>,
        ): FieldAccessor.Field<T, V> = properties(
            T::class.copyFunc, tProperty
        )

        inline fun <T : Any, reified T0 : T, reified T1 : Any, V> FieldAccessorHelper<T>.properties(
            tProperty: KProperty1<T, T1>,
            t1Property: KProperty1<T1, V>,
        ): FieldAccessor.Field<T, V> = properties(
            T0::class.copyFunc, tProperty,
            T1::class.copyFunc, t1Property
        )

        inline fun <T : Any, reified T0 : T, reified T1 : Any, reified T2 : Any, V> FieldAccessorHelper<T>.properties(
            tProperty: KProperty1<T, T1>,
            t1Property: KProperty1<T1, T2>,
            t2Property: KProperty1<T2, V>,
        ): FieldAccessor.Field<T, V> = properties(
            T0::class.copyFunc, tProperty,
            T1::class.copyFunc, t1Property,
            T2::class.copyFunc, t2Property
        )
        //</editor-fold>
    }
}
