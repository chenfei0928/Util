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
abstract class SpConvertSaver<SpValueType, FieldType>(
    internal val saver: AbsSpSaver.AbsSpDelegate<SpValueType>,
    spValueType: PreferenceType = saver.spValueType,
) : AbsSpSaver.AbsSpDelegate<FieldType?>(spValueType) {
    @Volatile
    private var cacheValue: Pair<SpValueType, FieldType>? = null

    final override fun obtainDefaultKey(property: KProperty<*>): String {
        return saver.obtainDefaultKey(property)
    }

    @Synchronized
    final override fun getValue(sp: SharedPreferences, key: String): FieldType? {
        return saver.getValue(sp, key)?.let {
            val cacheValue = cacheValue
            return if (cacheValue != null && cacheValue.first == it) {
                cacheValue.second
            } else {
                val t = onRead(it)
                this.cacheValue = it to t
                t
            }
        }
    }

    final override fun putValue(
        editor: SharedPreferences.Editor, key: String, value: FieldType & Any
    ) {
        val t = onSave(value)
        cacheValue = t to value
        saver.putValue(editor, key, t)
    }

    abstract fun onRead(value: SpValueType & Any): FieldType
    abstract fun onSave(value: FieldType & Any): SpValueType & Any
}
