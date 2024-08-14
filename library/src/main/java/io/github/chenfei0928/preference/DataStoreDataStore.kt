package io.github.chenfei0928.preference

import androidx.collection.ArrayMap
import androidx.datastore.core.DataStore
import androidx.preference.PreferenceDataStore
import io.github.chenfei0928.util.MapCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
 * 支持使用 [DataStore] 来存储首选项
 *
 * @param blockingWrite true为阻塞方式以 [runBlocking] 写入，false为使用 [launch] 写入
 *
 * @author chenf()
 * @date 2024-08-13 18:18
 */
@Suppress("TooManyFunctions")
class DataStoreDataStore<T : Any>(
    private val coroutineScope: CoroutineScope,
    private val dataStore: DataStore<T>,
    private val blockingWrite: Boolean = false,
) : PreferenceDataStore() {

    //<editor-fold desc="快速访问字段扩展" defaultstatus="collapsed">
    private val properties: MutableMap<String, Field<T, *>> = ArrayMap()

    /**
     * 判断一个字段是否已经注册
     */
    operator fun contains(field: Field<T, *>): Boolean = field.name in properties

    /**
     * 注册一个持久化字段
     *
     * @param V 值类型
     * @param field 字段说明
     */
    fun <V> property(
        field: Field<T, V>
    ): Field<T, V> = field.also {
        val name = field.name
        require(name !in properties) {
            "field name:$name is contain properties:${properties.keys}"
        }
        properties[name] = field
    }

    private inline fun <reified V> Map<String, Field<T, *>>.findByName(
        name: String
    ): Field<T, V> {
        @Suppress("UNCHECKED_CAST")
        return this[name] as Field<T, V>
    }

    private fun <V> Field<T, V>.set(value: V) {
        if (blockingWrite) {
            runBlocking(coroutineScope.coroutineContext) {
                dataStore.updateData {
                    set(it, value)
                }
            }
        } else {
            coroutineScope.launch {
                dataStore.updateData {
                    set(it, value)
                }
            }
        }
    }

    private fun <V> Field<T, V>.get(): V {
        return runBlocking(coroutineScope.coroutineContext) {
            val data = dataStore.data.first()
            get(data)
        }
    }
    //</editor-fold>

    //<editor-fold desc="put\get" defaultstatus="collapsed">
    override fun putString(key: String, value: String?) {
        properties.findByName<String?>(key).set(value)
    }

    override fun putStringSet(key: String, values: MutableSet<String>?) {
        properties.findByName<Set<String>?>(key).set(values)
    }

    override fun putInt(key: String, value: Int) {
        properties.findByName<Int>(key).set(value)
    }

    override fun putLong(key: String, value: Long) {
        properties.findByName<Long>(key).set(value)
    }

    override fun putFloat(key: String, value: Float) {
        properties.findByName<Float>(key).set(value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        properties.findByName<Boolean>(key).set(value)
    }

    override fun getString(key: String, defValue: String?): String? {
        return properties.findByName<String?>(key).get() ?: defValue
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        return properties.findByName<Set<String>?>(key).get()?.toMutableSet() ?: defValues
    }

    override fun getInt(key: String, defValue: Int): Int {
        return properties.findByName<Int?>(key).get() ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return properties.findByName<Long?>(key).get() ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return properties.findByName<Float?>(key).get() ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return properties.findByName<Boolean?>(key).get() ?: defValue
    }
    //</editor-fold>

    interface Field<T, V> {
        val name: String
        fun get(data: T): V
        fun set(data: T, value: V): T
    }

    //<editor-fold desc="data class copy方法" defaultstatus="collapsed">
    private val dataClassCopyFuncCache = MapCache<KClass<*>, KFunction<*>>(ArrayMap()) { kClass ->
        require(kClass.isData) { "only data class can use copyFunc: $this" }
        val parameters = kClass.primaryConstructor?.valueParameters
        kClass.functions.find {
            !it.isSuspend
                    && it.name == "copy"
                    && it.returnType.classifier == this
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
    fun <T0 : Any> cacheCopyFunc(clazz: KClass<T0>, copyFunc: KFunction<T0>) {
        dataClassCopyFuncCache[clazz] = copyFunc
    }

    inline fun <reified T0 : Any> cacheCopyFunc(copyFunc: KFunction<T0>) {
        cacheCopyFunc(T0::class, copyFunc)
    }

    /**
     * 获取该data class的copy函数
     */
    @Suppress("kotlin:S6531", "UNCHECKED_CAST")
    val <T : Any> KClass<T>.copyFunc: KFunction<T>
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
    fun <V> properties(
        tCopyFunc: KFunction<T>,
        tProperty: KProperty1<T, V>,
    ): Field<T, V> = KPropertyDataCopyField(
        tCopyFunc, tProperty
    ).let(::property)

    inline fun <reified T0 : T, V : Any> properties(
        tProperty: KProperty1<T, V>,
    ): Field<T, V> = properties(
        T0::class.copyFunc, tProperty
    )

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
    ): Field<T, V> = KPropertyDataCopyWrapper(
        KPropertyDataCopyField(
            tCopyFunc, tProperty
        ), t1CopyFunc, t1Property
    ).let(::property)

    inline fun <reified T0 : T, reified T1 : Any, V> properties(
        tProperty: KProperty1<T, T1>,
        t1Property: KProperty1<T1, V>,
    ): Field<T, V> = properties(
        T0::class.copyFunc, tProperty,
        T1::class.copyFunc, t1Property
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
    @Suppress("LongParameterList")
    fun <T1 : Any, T2 : Any, V> properties(
        tCopyFunc: KFunction<T>,
        tProperty: KProperty1<T, T1>,
        t1CopyFunc: KFunction<T1>,
        t1Property: KProperty1<T1, T2>,
        t2CopyFunc: KFunction<T2>,
        t2Property: KProperty1<T2, V>,
    ): Field<T, V> = KPropertyDataCopyWrapper(
        KPropertyDataCopyWrapper(
            KPropertyDataCopyField(
                tCopyFunc, tProperty
            ), t1CopyFunc, t1Property
        ), t2CopyFunc, t2Property
    ).let(::property)

    inline fun <reified T0 : T, reified T1 : Any, reified T2 : Any, V> properties(
        tProperty: KProperty1<T, T1>,
        t1Property: KProperty1<T1, T2>,
        t2Property: KProperty1<T2, V>,
    ): Field<T, V> = properties(
        T0::class.copyFunc, tProperty,
        T1::class.copyFunc, t1Property,
        T2::class.copyFunc, t2Property
    )

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
    ) : Field<T, V> {
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
        private val outerField: Field<T, T1>,
        private val innerCopyFunc: KFunction<T1>,
        private val innerProperty: KProperty1<T1, V>
    ) : Field<T, V> {
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
    fun <V> properties(
        property: KMutableProperty1<T, V>
    ): Field<T, V> = KMutablePropertyField(
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
    fun <T1, V> properties(
        property0: KMutableProperty1<T, T1>,
        property1: KMutableProperty1<T1, V>,
    ): Field<T, V> = KMutablePropertyWrapper(
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
    fun <T1, T2, V> properties(
        property0: KMutableProperty1<T, T1>,
        property1: KMutableProperty1<T1, T2>,
        property2: KMutableProperty1<T2, V>,
    ): Field<T, V> = KMutablePropertyWrapper(
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
    class KMutablePropertyField<T, V>(
        private val property: KMutableProperty1<T, V>
    ) : Field<T, V> {
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
    class KMutablePropertyWrapper<T, T1, V>(
        private val outerField: Field<T, T1>,
        private val innerProperty: KMutableProperty1<T1, V>,
    ) : Field<T, V> {
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
    fun <T1, V> properties(
        outerField: Field<T, T1>,
        innerField: Field<T1, V>,
    ): Field<T, V> = FieldWrapper(outerField, innerField).let(::property)


    /**
     * 使用只读属性与该data class类的 copy 方法来进行读写的字段
     *
     * @param T 宿主类类型
     * @param T1 中间宿主类类型
     * @param V 字段类型
     * @property outerField 外部字段到中间宿主类型字段的访问方法
     * @property innerField 中间宿主的子字段的访问方法
     */
    class FieldWrapper<T, T1, V>(
        private val outerField: Field<T, T1>,
        private val innerField: Field<T1, V>,
    ) : Field<T, V> {
        override val name: String = outerField.name + "_" + innerField.name

        override fun get(data: T): V {
            return innerField.get(outerField.get(data))
        }

        override fun set(data: T, value: V): T {
            val t1 = outerField.get(data)
            return outerField.set(data, innerField.set(t1, value))
        }
    }

    /**
     * 通过自定义[getter]、[setter]来访问字段
     *
     * @param T 宿主类类型
     * @param V 字段类型
     * @param name 字段名称
     * @param getter 访问器
     * @param setter 修改器
     */
    inline fun <T, V> field(
        name: String,
        crossinline getter: (data: T) -> V,
        crossinline setter: (data: T, value: V) -> T,
    ): Field<T, V> = object : Field<T, V> {
        override val name: String = name

        override fun get(data: T): V {
            return getter(data)
        }

        override fun set(data: T, value: V): T {
            return setter(data, value)
        }
    }
    //</editor-fold>
}
