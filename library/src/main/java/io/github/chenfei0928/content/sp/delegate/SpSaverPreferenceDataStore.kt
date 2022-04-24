package io.github.chenfei0928.content.sp.delegate

import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceManager
import io.github.chenfei0928.collection.mapToArray
import io.github.chenfei0928.content.sp.AbsSpSaver
import kotlin.reflect.KMutableProperty0

/**
 * 用于对spSaver的扩展，以优化[PreferenceManager]的访问
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-04-24 10:42
 */
class SpSaverPreferenceDataStore(
    private val properties: Array<Field<*>>
) : PreferenceDataStore() {

    private inline fun <reified T> Array<Field<*>>.findByName(name: String): Field<T> {
        return find { it.name == name } as Field<T>
    }

    override fun putString(key: String, value: String?) {
        properties.findByName<String>(key).setter(value)
    }

    override fun putStringSet(key: String, values: MutableSet<String>?) {
        properties.findByName<Set<String>>(key).setter(values)
    }

    override fun putInt(key: String, value: Int) {
        properties.findByName<Int>(key).setter(value)
    }

    override fun putLong(key: String, value: Long) {
        properties.findByName<Long>(key).setter(value)
    }

    override fun putFloat(key: String, value: Float) {
        properties.findByName<Float>(key).setter(value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        properties.findByName<Boolean>(key).setter(value)
    }

    override fun getString(key: String, defValue: String?): String? {
        return properties.findByName<String>(key).getter() ?: defValue
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        return properties.findByName<MutableSet<String>>(key).getter() ?: defValues
    }

    override fun getInt(key: String, defValue: Int): Int {
        return properties.findByName<Int>(key).getter() ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return properties.findByName<Long>(key).getter() ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return properties.findByName<Float>(key).getter() ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return properties.findByName<Boolean>(key).getter() ?: defValue
    }

    data class Field<T>(
        val name: String,
        val getter: () -> T?,
        val setter: (T?) -> Unit
    )
}

fun AbsSpSaver.toPreferenceDataStore(
    vararg properties: KMutableProperty0<*>
): PreferenceDataStore {
    return SpSaverPreferenceDataStore(properties.mapToArray {
        findMutablePropertyField(it)
    })
}

private fun AbsSpSaver.findMutablePropertyField(property0: KMutableProperty0<*>): SpSaverPreferenceDataStore.Field<*> {
    val name = getPropertySpKeyName(property0)
    var delegate = property0.getDelegate()
    var defaultValue: Any? = null
    while (true) {
        when (delegate) {
            is DefaultValueSpDelete<*> -> {
                // 默认值一般是作为最后的装饰，会最先触发，其默认值要保存下来
                defaultValue = delegate.defaultValue
                delegate = delegate.saver
            }
            is SpConvertSaver<*, *> -> {
                delegate as SpConvertSaver<Any, Any?>
                // 转换器装饰器，将其解装饰，并处理默认值
                defaultValue = delegate.onSave(defaultValue)
                delegate = delegate.saver
            }
            is AbsSpSaver.AbsSpDelegate<*> -> {
                delegate as AbsSpSaver.AbsSpDelegate<Any?>
                return SpSaverPreferenceDataStore.Field(
                    name = name,
                    getter = {
                        delegate.getValue(this, property0) ?: defaultValue
                    },
                    setter = {
                        delegate.setValue(this, property0, it ?: defaultValue)
                    }
                )
            }
            else -> throw IllegalArgumentException()
        }
    }
}
