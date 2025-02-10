package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import kotlin.reflect.KProperty

/**
 * 使nullable的字段委托拥有默认值的装饰器
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-12 16:39
 */
abstract class DefaultValueSpDelete<SpSaver : AbsSpSaver<SpSaver>, T : Any>(
    internal val saver: AbsSpSaver.AbsSpDelegateImpl<SpSaver, T?>,
) : AbsSpSaver.AbsSpDelegateImpl<SpSaver, T>(saver.spValueType) {
    abstract val defaultValue: T

    final override fun obtainDefaultKey(property: KProperty<*>): String {
        return saver.obtainDefaultKey(property)
    }

    final override fun getValue(sp: SharedPreferences, key: String): T {
        return if (sp.contains(key)) {
            saver.getValue(sp, key) ?: defaultValue
        } else {
            defaultValue
        }
    }

    final override fun putValue(editor: SharedPreferences.Editor, key: String, value: T) {
        saver.putValue(editor, key, value)
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver>, T : Any> AbsSpSaver.AbsSpDelegateImpl<SpSaver, T?>.defaultValue(
            defaultValue: T
        ): DefaultValueSpDelete<SpSaver, T> =
            object : DefaultValueSpDelete<SpSaver, T>(this@defaultValue) {
                override val defaultValue: T = defaultValue
            }

        inline fun <SpSaver : AbsSpSaver<SpSaver>, T : Any> AbsSpSaver.AbsSpDelegateImpl<SpSaver, T?>.defaultValue(
            crossinline defaultValue: () -> T
        ): DefaultValueSpDelete<SpSaver, T> =
            object : DefaultValueSpDelete<SpSaver, T>(this@defaultValue) {
                override val defaultValue: T = defaultValue()
            }
    }
}
