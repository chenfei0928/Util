package io.github.chenfei0928.preference.base

import androidx.collection.ArraySet
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
abstract class BasePreferenceDataStore<T : Any>(
    private val fieldAccessor: FieldAccessor<T> = FieldAccessor.Impl(false),
) : PreferenceDataStore() {

    protected abstract fun <V> FieldAccessor.Field<T, V>.set(value: V)
    protected abstract fun <V> FieldAccessor.Field<T, V>.get(): V

    /**
     * 将 preference screen 数据持久化到本地，扩展支持了[PreferenceType]的枚举
     */
    @Suppress("UNCHECKED_CAST")
    protected fun <V> FieldAccessor.Field<T, V>.setValue(
        it: T, value: V
    ): T = when (val vType = vType) {
        is PreferenceType.EnumNameString<*> -> {
            // 将preference的字符串转换为Enum设置给field
            set(it, vType.forName(value as String) as V)
        }
        is PreferenceType.BaseEnumNameStringCollection<*, *> -> {
            // 将preference的字符串集合转换为Enum集合设置给field
            // forName时使用field的字段类型
            set(it, vType.forNames(value as Collection<String>, true) as V)
        }
        is PreferenceType.Native -> {
            // preference原生支持的类型，直接设置
            set(it, value)
        }
        is PreferenceType.Struct<*> -> throw IllegalArgumentException(
            "Not support type: $this"
        )
    }

    /**
     * 将本地持久化数据读取给 preference screen，扩展支持了[PreferenceType]的枚举
     */
    @Suppress("UNCHECKED_CAST")
    protected fun <V> FieldAccessor.Field<T, V>.getValue(
        data: T
    ): V = when (vType) {
        is PreferenceType.EnumNameString<*> -> {
            // 将field的Enum转换为preference的字符串
            (get(data) as Enum<*>).name as V
        }
        is PreferenceType.BaseEnumNameStringCollection<*, *> -> {
            // 将field的Enum集合转换为preference的字符串Set
            val enums = get(data) as Collection<Enum<*>>
            enums.mapTo(ArraySet(enums.size)) { it.name } as V
        }
        is PreferenceType.Native -> {
            // preference原生支持的类型，直接返回
            get(data)
        }
        is PreferenceType.Struct<*> -> throw IllegalArgumentException(
            "Not support type: $this"
        )
    }

    //<editor-fold desc="put\get" defaultstatus="collapsed">
    override fun putString(key: String, value: String?) {
        fieldAccessor.findByName<String?>(key).set(value)
    }

    override fun putStringSet(key: String, values: MutableSet<String>?) {
        fieldAccessor.findByName<Set<String>?>(key).set(values)
    }

    override fun putInt(key: String, value: Int) {
        fieldAccessor.findByName<Int>(key).set(value)
    }

    override fun putLong(key: String, value: Long) {
        fieldAccessor.findByName<Long>(key).set(value)
    }

    override fun putFloat(key: String, value: Float) {
        fieldAccessor.findByName<Float>(key).set(value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        fieldAccessor.findByName<Boolean>(key).set(value)
    }

    override fun getString(key: String, defValue: String?): String? {
        return fieldAccessor.findByName<String?>(key).get() ?: defValue
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        return fieldAccessor.findByName<Set<String>?>(key).get()?.let {
            if (it is MutableSet) it else it.toMutableSet()
        } ?: defValues
    }

    override fun getInt(key: String, defValue: Int): Int {
        return fieldAccessor.findByName<Int?>(key).get() ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return fieldAccessor.findByName<Long?>(key).get() ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return fieldAccessor.findByName<Float?>(key).get() ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return fieldAccessor.findByName<Boolean?>(key).get() ?: defValue
    }
    //</editor-fold>
}
