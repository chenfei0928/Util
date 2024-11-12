package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import kotlin.reflect.KProperty

/**
 * sp存储转换器，用于将sp不支持的数据结构转换为sp支持的数据结构
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2020-09-03 13:38
 */
abstract class SpConvertSaver<SpValueType, FieldType>(
    internal val saver: AbsSpSaver.AbsSpDelegate<SpValueType>
) : AbsSpSaver.AbsSpDelegate<FieldType>() {

    override fun obtainDefaultKey(property: KProperty<*>): String {
        return saver.obtainDefaultKey(property)
    }

    override fun getValue(sp: SharedPreferences, key: String): FieldType {
        return onRead(saver.getValue(sp, key))
    }

    override fun putValue(editor: SharedPreferences.Editor, key: String, value: FieldType) {
        saver.putValue(editor, key, onSave(value))
    }

    abstract fun onRead(value: SpValueType): FieldType
    abstract fun onSave(value: FieldType): SpValueType
}
