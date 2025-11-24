package io.github.chenfei0928.preference.base

import androidx.preference.PreferenceDataStore
import io.github.chenfei0928.content.sp.saver.PreferenceType

/**
 * 首选项值访问
 *
 * 封装了对 [FieldAccessor.Field] 的各个类型[FieldAccessor.Field.vType]读写类型判断处理
 *
 * @author chenf()
 * @date 2024-12-17 15:41
 */
@Suppress("TooManyFunctions")
abstract class BaseFieldAccessorCache<T : Any>(
    private val fieldAccessor: FieldAccessor<T> = FieldAccessor.Impl(false),
) {

    /**
     * 设置字段值，子类需要实现该方法以支持不同类型的字段值的读写。
     *
     * 用于 [PreferenceDataStore] 的读写，输入类型为其对应的 value 的类型
     */
    protected abstract fun <V> FieldAccessor.Field<T, V>.setToStorage(value: V)

    /**
     * 获取字段值，子类需要实现该方法以支持不同类型的字段值的读写。
     *
     * 用于 [PreferenceDataStore] 的读写，输出类型为其要求的 value 的类型
     */
    protected abstract fun <V> FieldAccessor.Field<T, V>.getFromStorage(): V

    /**
     * 将 preference screen 数据 [value] 设置给 [data] 中，扩展支持了[PreferenceType]的枚举
     */
    @Suppress("UNCHECKED_CAST")
    protected fun <V> FieldAccessor.Field<T, V>.setValue(
        data: T, value: V
    ): T = when (val vType = vType) {
        is PreferenceType.EnumNameString<*> -> {
            // 将preference的字符串转换为Enum设置给field
            set(data, vType.forName(value as String) as V)
        }
        is PreferenceType.BaseEnumNameStringCollection<*, *> -> {
            // 将preference的字符串集合转换为Enum集合设置给field
            // forName时使用field的字段类型
            set(data, vType.forNames(value as Collection<String>, true) as V)
        }
        is PreferenceType.Native<*> -> {
            // preference原生支持的类型，直接设置
            set(data, value)
        }
        is PreferenceType.Struct<*> -> throw IllegalArgumentException(
            "Not support type: $this"
        )
    }

    /**
     * 从本地持久化数据 [data] 读取给 preference screen，扩展支持了[PreferenceType]的枚举
     */
    @Suppress("UNCHECKED_CAST")
    protected fun <V> FieldAccessor.Field<T, V>.getValue(
        data: T
    ): V = when (vType) {
        is PreferenceType.EnumNameString<*> -> {
            // 将field的Enum转换为preference的字符串
            val vType = vType as PreferenceType.EnumNameString<Enum<*>>
            @Suppress("kotlin:S6531")
            vType.toName(get(data) as Enum<*>) as V
        }
        is PreferenceType.BaseEnumNameStringCollection<*, *> -> {
            // 将field的Enum集合转换为preference的字符串Set
            val vType = vType as PreferenceType.BaseEnumNameStringCollection<Enum<*>, *>
            val enums = get(data) as Collection<Enum<*>>
            val names = vType.toNames(enums, false)
            val namesSet = names as? Set<String> ?: names.toMutableSet()
            namesSet as V
        }
        is PreferenceType.Native<*> -> {
            // preference原生支持的类型，直接返回
            get(data)
        }
        is PreferenceType.Struct<*> -> throw IllegalArgumentException(
            "Not support type: $this"
        )
    }

    val preferenceDataStore: PreferenceDataStore
        get() = object : PreferenceDataStore() {
            //<editor-fold desc="put\get" defaultstatus="collapsed">
            override fun putString(key: String, value: String?) {
                fieldAccessor.findByName<String?>(key).setToStorage(value)
            }

            override fun putStringSet(key: String, values: MutableSet<String>?) {
                fieldAccessor.findByName<Set<String>?>(key).setToStorage(values)
            }

            override fun putInt(key: String, value: Int) {
                fieldAccessor.findByName<Int>(key).setToStorage(value)
            }

            override fun putLong(key: String, value: Long) {
                fieldAccessor.findByName<Long>(key).setToStorage(value)
            }

            override fun putFloat(key: String, value: Float) {
                fieldAccessor.findByName<Float>(key).setToStorage(value)
            }

            override fun putBoolean(key: String, value: Boolean) {
                fieldAccessor.findByName<Boolean>(key).setToStorage(value)
            }

            override fun getString(key: String, defValue: String?): String? {
                return fieldAccessor.findByName<String?>(key).getFromStorage() ?: defValue
            }

            override fun getStringSet(
                key: String, defValues: MutableSet<String>?
            ): MutableSet<String>? {
                return fieldAccessor.findByName<Set<String>?>(key).getFromStorage()?.let {
                    it as? MutableSet ?: it.toMutableSet()
                } ?: defValues
            }

            override fun getInt(key: String, defValue: Int): Int {
                return fieldAccessor.findByName<Int?>(key).getFromStorage() ?: defValue
            }

            override fun getLong(key: String, defValue: Long): Long {
                return fieldAccessor.findByName<Long?>(key).getFromStorage() ?: defValue
            }

            override fun getFloat(key: String, defValue: Float): Float {
                return fieldAccessor.findByName<Float?>(key).getFromStorage() ?: defValue
            }

            override fun getBoolean(key: String, defValue: Boolean): Boolean {
                return fieldAccessor.findByName<Boolean?>(key).getFromStorage() ?: defValue
            }
            //</editor-fold>
        }
}
