package io.github.chenfei0928.content.sp.saver.convert

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.AbsSpSaver
import io.github.chenfei0928.content.sp.saver.PreferenceType
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
private constructor(
    final override val saver: AbsSpSaver.Delegate<SpSaver, T?>,
) : AbsSpSaver.AbsSpDelegate<SpSaver, Sp, Ed, T & Any>,
    AbsSpSaver.DefaultValue<T & Any>,
    AbsSpSaver.Decorate<SpSaver, T?> {
    final override val spValueType: PreferenceType = saver.spValueType
    final override fun obtainDefaultKey(property: KProperty<*>): String =
        saver.obtainDefaultKey(property)

    override fun getValue(thisRef: SpSaver, property: KProperty<*>): T & Any {
        return if (thisRef.sp.contains(saver.obtainDefaultKey(property))) {
            saver.getValue(thisRef, property) ?: defaultValue
        } else {
            defaultValue
        }
    }

    override fun setValue(thisRef: SpSaver, property: KProperty<*>, value: T & Any) {
        saver.setValue(thisRef, property, value)
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                T>
                AbsSpSaver.Delegate<SpSaver, T?>.defaultValue(
            defaultValue: T & Any
        ): DefaultValueSpDelete<SpSaver, Sp, Ed, T> =
            object : DefaultValueSpDelete<SpSaver, Sp, Ed, T>(this@defaultValue) {
                override val defaultValue: T & Any = defaultValue
            }

        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                T>
                AbsSpSaver.Delegate<SpSaver, T?>.defaultLazyValue(
            defaultValue: () -> T & Any
        ): DefaultValueSpDelete<SpSaver, Sp, Ed, T> =
            object : DefaultValueSpDelete<SpSaver, Sp, Ed, T>(this@defaultLazyValue) {
                override val defaultValue: T & Any by lazy(LazyThreadSafetyMode.NONE, defaultValue)
            }
    }
}
