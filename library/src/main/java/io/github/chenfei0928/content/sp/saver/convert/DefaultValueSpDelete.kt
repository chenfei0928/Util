package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver

/**
 * 使nullable的字段委托拥有默认值的装饰器
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-12 16:39
 */
class DefaultValueSpDelete<T>(
    saver: AbsSpSaver.AbsSpDelegate<T?>,
    internal val defaultValue: T,
) : SpConvertSaver<T?, T>(saver) {

    override fun getValue(sp: SharedPreferences, key: String): T {
        return if (sp.contains(key)) {
            super.getValue(sp, key)
        } else {
            defaultValue
        }
    }

    override fun onRead(value: T?): T {
        return value ?: defaultValue
    }

    override fun onSave(value: T): T? {
        return value
    }

    companion object {
        fun <T> AbsSpSaver.AbsSpDelegate<T?>.defaultValue(defaultValue: T): DefaultValueSpDelete<T> =
            DefaultValueSpDelete(this, defaultValue)
    }
}
