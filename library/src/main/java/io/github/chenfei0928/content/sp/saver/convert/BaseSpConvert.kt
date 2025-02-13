package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
import kotlin.reflect.KProperty

/**
 * sp存储转换器，用于将sp不支持的数据结构转换为sp支持的数据结构
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-09-03 13:38
 */
abstract class BaseSpConvert<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        SpValueType,
        FieldType>
constructor(
    final override val saver: AbsSpSaver.Delegate<SpSaver, SpValueType>,
    final override val spValueType: PreferenceType,
) : AbsSpSaver.AbsSpDelegate<SpSaver, Sp, Ed, FieldType?>,
    AbsSpSaver.Decorate<SpSaver, SpValueType> {
    @Volatile
    private var cacheValue: Pair<SpValueType, FieldType>? = null

    final override fun obtainDefaultKey(property: KProperty<*>): String {
        return saver.obtainDefaultKey(property)
    }

    @Synchronized
    override fun getValue(thisRef: SpSaver, property: KProperty<*>): FieldType? {
        return saver.getValue(thisRef, property)?.let {
            val cacheValue = cacheValue
            return if (cacheValue != null && cacheValue.first == it) {
                cacheValue.second
            } else {
                val t = onRead(it)
                this.cacheValue = it to t
                t
            }
        } ?: if (this is AbsSpSaver.DefaultValue<*>) {
            @Suppress("UNCHECKED_CAST")
            defaultValue as FieldType
        } else null
    }

    override fun setValue(thisRef: SpSaver, property: KProperty<*>, value: FieldType?) {
        if (value == null) {
            val key = saver.obtainDefaultKey(property)
            thisRef.editor.remove(key)
        } else {
            val t = onSave(value)
            cacheValue = t to value
            saver.setValue(thisRef, property, t)
        }
    }

    abstract fun onRead(value: SpValueType & Any): FieldType
    abstract fun onSave(value: FieldType & Any): SpValueType & Any
}
