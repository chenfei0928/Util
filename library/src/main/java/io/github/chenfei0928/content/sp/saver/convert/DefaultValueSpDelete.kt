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
abstract class DefaultValueSpDelete<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        T>
constructor(
    internal val saver: AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, T?>,
) : AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, T & Any>(saver.spValueType) {
    abstract val defaultValue: T & Any

    final override fun obtainDefaultKey(property: KProperty<*>): String {
        return saver.obtainDefaultKey(property)
    }

    final override fun getValue(sp: Sp, key: String): T & Any {
        return if (sp.contains(key)) {
            saver.getValue(sp, key) ?: defaultValue
        } else {
            defaultValue
        }
    }

    final override fun putValue(editor: Ed, key: String, value: T & Any) {
        saver.putValue(editor, key, value)
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                T>
                AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, T?>.defaultValue(
            defaultValue: T & Any
        ): DefaultValueSpDelete<SpSaver, Sp, Ed, T> =
            object : DefaultValueSpDelete<SpSaver, Sp, Ed, T>(this@defaultValue) {
                override val defaultValue: T & Any = defaultValue
            }

        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                T>
                AbsSpSaver.AbsSpDelegateImpl<SpSaver, Sp, Ed, T?>.defaultLazyValue(
            defaultValue: () -> T & Any
        ): DefaultValueSpDelete<SpSaver, Sp, Ed, T> =
            object : DefaultValueSpDelete<SpSaver, Sp, Ed, T>(this@defaultLazyValue) {
                override val defaultValue: T & Any by lazy(defaultValue)
            }
    }
}
