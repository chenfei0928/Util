package io.github.chenfei0928.preference.sp

import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceManager
import io.github.chenfei0928.collection.mapToArray
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.convert.DefaultValueSpDelete
import io.github.chenfei0928.content.sp.saver.convert.SpConvertSaver
import io.github.chenfei0928.content.sp.saver.getPropertySpKeyName
import kotlin.reflect.KMutableProperty0

/**
 * 用于对spSaver的扩展，以优化[PreferenceManager]的访问
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-04-24 10:42
 */
@Suppress("TooManyFunctions")
class SpSaverPreferenceDataStore(
    private val properties: Array<Field<*>>
) : PreferenceDataStore() {

    private inline fun <reified T> Array<Field<*>>.findByName(name: String): Field<T> {
        @Suppress("UNCHECKED_CAST")
        return find { it.name == name } as Field<T>
    }

    //<editor-fold desc="put/get" defaultstatus="collapsed">
    override fun putString(key: String, value: String?) {
        properties.findByName<String>(key).set(value)
    }

    override fun putStringSet(key: String, values: MutableSet<String>?) {
        properties.findByName<Set<String>>(key).set(values)
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
        return properties.findByName<String>(key).get() ?: defValue
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        return properties.findByName<MutableSet<String>>(key).get() ?: defValues
    }

    override fun getInt(key: String, defValue: Int): Int {
        return properties.findByName<Int>(key).get() ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return properties.findByName<Long>(key).get() ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return properties.findByName<Float>(key).get() ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return properties.findByName<Boolean>(key).get() ?: defValue
    }
    //</editor-fold>

    interface Field<T> {
        val name: String
        fun get(): T?
        fun set(value: T?)
    }

    companion object {
        fun AbsSpSaver.toPreferenceDataStore(
            vararg properties: KMutableProperty0<*>
        ): PreferenceDataStore = SpSaverPreferenceDataStore(properties.mapToArray {
            findMutablePropertyField(it)
        })

        private fun AbsSpSaver.findMutablePropertyField(
            property0: KMutableProperty0<*>
        ): Field<*> {
            val name = getPropertySpKeyName(property0)
            var delegate: Any? = property0.getDelegate()
            var defaultValue: Any? = null
            while (true) {
                when (delegate) {
                    is DefaultValueSpDelete<*> -> {
                        // 默认值一般是作为最后的装饰，会最先触发，其默认值要保存下来
                        defaultValue = delegate.defaultValue
                        delegate = delegate.saver
                    }
                    is SpConvertSaver<*, *> -> {
                        @Suppress("UNCHECKED_CAST")
                        delegate as SpConvertSaver<Any, Any?>
                        // 转换器装饰器，将其解装饰，并处理默认值
                        defaultValue = delegate.onSave(defaultValue)
                        delegate = delegate.saver
                    }
                    is AbsSpSaver.AbsSpDelegate<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        delegate as AbsSpSaver.AbsSpDelegate<Any?>
                        val localDelegate: AbsSpSaver.AbsSpDelegate<Any?> = delegate
                        val spSaver = this@findMutablePropertyField
                        return object : Field<Any> {
                            override val name: String = name

                            override fun get(): Any? {
                                return localDelegate.getValue(spSaver, property0) ?: defaultValue
                            }

                            override fun set(value: Any?) {
                                localDelegate.setValue(spSaver, property0, value)
                            }
                        }
                    }
                    else -> throw IllegalArgumentException("不支持的委托类型: ${delegate?.javaClass} $delegate")
                }
            }
        }
    }
}
