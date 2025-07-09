package io.github.chenfei0928.preference.base

import androidx.collection.ArrayMap
import io.github.chenfei0928.content.sp.saver.PreferenceType
import io.github.chenfei0928.os.Debug
import io.github.chenfei0928.util.MapCache
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
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
interface DataCopyClassFieldAccessor<T> : FieldAccessor<T> {

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
    fun <V> property(
        tCopyFunc: KFunction<T>,
        tProperty: KProperty1<T, V>,
        vType: PreferenceType<V>,
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
    @Suppress("LongParameterList")
    fun <T1, V> property(
        tCopyFunc: KFunction<T>,
        tProperty: KProperty1<T, T1>,
        t1CopyFunc: KFunction<T1>,
        t1Property: KProperty1<T1, V>,
        vType: PreferenceType<V>,
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
    fun <T1, T2, V> property(
        tCopyFunc: KFunction<T>,
        tProperty: KProperty1<T, T1>,
        t1CopyFunc: KFunction<T1>,
        t1Property: KProperty1<T1, T2>,
        t2CopyFunc: KFunction<T2>,
        t2Property: KProperty1<T2, V>,
        vType: PreferenceType<V>,
    ): FieldAccessor.Field<T, V>
    //</editor-fold>

    open class Impl<T : Any>(
        readCache: Boolean
    ) : FieldAccessor.Impl<T>(readCache), DataCopyClassFieldAccessor<T> {
        //<editor-fold desc="data class copy方法" defaultstatus="collapsed">
        private val dataClassCopyFuncCache =
            MapCache.Basic<KClass<*>, KFunction<*>>(ArrayMap()) { kClass ->
                require(kClass.isData) { "only data class can use copyFunc: $this" }
                Debug.countTime(
                    TAG, "dataClassCopyFuncCache: get copy function by reflect: $kClass"
                ) {
                    val parameters = kClass.primaryConstructor?.valueParameters
                    kClass.functions.find {
                        !it.isSuspend
                                && it.name == "copy"
                                && it.returnType.classifier == kClass
                                && !it.returnType.isMarkedNullable
                                && it.valueParameters == parameters
                    } ?: throw IllegalArgumentException(
                        "Cant found copy function in $kClass"
                    )
                }
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
        protected open fun <T, V> field(
            tCopyFunc: KFunction<T>,
            tProperty: KProperty1<T, V>,
            vType: PreferenceType<V>? = null,
        ): FieldAccessor.Field<T, V> = KPropertyDataCopyField<T, V>(
            tCopyFunc, tProperty, vType
        )

        /**
         * 注册一个data class的字段
         *
         * @param V 值类型
         * @param tCopyFunc [T] 类型的copy函数
         * @param tProperty [T]的某个字段
         * @return 该字段描述说明
         */
        override fun <V> property(
            tCopyFunc: KFunction<T>,
            tProperty: KProperty1<T, V>,
            vType: PreferenceType<V>,
        ): FieldAccessor.Field<T, V> = field(
            tCopyFunc, tProperty, vType
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
        override fun <T1, V> property(
            tCopyFunc: KFunction<T>,
            tProperty: KProperty1<T, T1>,
            t1CopyFunc: KFunction<T1>,
            t1Property: KProperty1<T1, V>,
            vType: PreferenceType<V>,
        ): FieldAccessor.Field<T, V> = property(
            field(tCopyFunc, tProperty),
            field(t1CopyFunc, t1Property, vType),
        )

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
        override fun <T1, T2, V> property(
            tCopyFunc: KFunction<T>,
            tProperty: KProperty1<T, T1>,
            t1CopyFunc: KFunction<T1>,
            t1Property: KProperty1<T1, T2>,
            t2CopyFunc: KFunction<T2>,
            t2Property: KProperty1<T2, V>,
            vType: PreferenceType<V>,
        ): FieldAccessor.Field<T, V> = property(
            field(tCopyFunc, tProperty),
            field(t1CopyFunc, t1Property),
            field(t2CopyFunc, t2Property, vType),
        )

        /**
         * 使用只读属性与该data class类的 copy 方法来进行读写的字段
         *
         * @param T 宿主类类型
         * @param V 字段类型
         * @property copyFunc [T] 类的copy函数
         * @property property [T]的某个字段
         */
        private class KPropertyDataCopyField<T, V>(
            private val copyFunc: KFunction<T>,
            private val property: KProperty1<T, V>,
            vType: PreferenceType<V>? = null,
        ) : FieldAccessor.Field<T, V> {
            override val pdsKey: String = property.name
            override val vType: PreferenceType<V> by lazy(LazyThreadSafetyMode.NONE) {
                vType ?: PreferenceType.forType(property.returnType)
            }

            // 这两个方法要读取data class metadata元数据，耗时较久
            private val instanceParameter: KParameter by lazy(LazyThreadSafetyMode.NONE) {
                copyFunc.instanceParameter!!
            }
            private val kParameter: KParameter by lazy(LazyThreadSafetyMode.NONE) {
                copyFunc.parameters.find { it.name == pdsKey }!!
            }

            override fun get(data: T): V = property.get(data)
            override fun set(data: T, value: V): T = copyFunc.callBy(
                mapOf(instanceParameter to data, kParameter to value)
            )

            override fun toString(): String = "KPropertyDataCopyField($pdsKey:$vType)"
        }
        //</editor-fold>
    }

    companion object {
        private const val TAG = "FieldAccessorHelper"

        //<editor-fold desc="data class copy方法" defaultstatus="collapsed">
        inline fun <reified T : Any> DataCopyClassFieldAccessor<*>.cacheCopyFunc(copyFunc: KFunction<T>) {
            cacheCopyFunc(T::class, copyFunc)
        }
        //</editor-fold>

        //<editor-fold desc="给data class的字段拷贝访问方法" defaultstatus="collapsed">
        inline fun <reified T : Any, reified V : Any> DataCopyClassFieldAccessor<T>.property(
            tProperty: KProperty1<T, V>,
        ): FieldAccessor.Field<T, V> = property(
            T::class.copyFunc, tProperty, PreferenceType.forType<V>()
        )

        inline fun <reified T : Any, reified T1 : Any, reified V> DataCopyClassFieldAccessor<T>.property(
            tProperty: KProperty1<T, T1>,
            t1Property: KProperty1<T1, V>,
        ): FieldAccessor.Field<T, V> = property(
            T::class.copyFunc, tProperty,
            T1::class.copyFunc, t1Property,
            PreferenceType.forType<V>()
        )

        inline fun <reified T : Any, reified T1 : Any, reified T2 : Any, reified V>
                DataCopyClassFieldAccessor<T>.property(
            tProperty: KProperty1<T, T1>,
            t1Property: KProperty1<T1, T2>,
            t2Property: KProperty1<T2, V>,
        ): FieldAccessor.Field<T, V> = property(
            T::class.copyFunc, tProperty,
            T1::class.copyFunc, t1Property,
            T2::class.copyFunc, t2Property,
            PreferenceType.forType<V>()
        )
        //</editor-fold>
    }
}
