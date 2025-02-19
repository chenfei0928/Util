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
        V>
private constructor(
    final override val saver: AbsSpSaver.Delegate<SpSaver, V?>,
) : AbsSpSaver.AbsSpDelegate<SpSaver, Sp, Ed, V & Any>,
    AbsSpSaver.DefaultValue<V & Any>,
    AbsSpSaver.Decorate<SpSaver, V?> {
    final override val spValueType: PreferenceType = saver.spValueType
    final override fun getLocalStorageKey(property: KProperty<*>): String =
        saver.getLocalStorageKey(property)

    override fun getValue(thisRef: SpSaver, property: KProperty<*>): V & Any {
        return if (property in thisRef) {
            saver.getValue(thisRef, property) ?: defaultValue
        } else {
            defaultValue
        }
    }

    override fun setValue(thisRef: SpSaver, property: KProperty<*>, value: V & Any) {
        saver.setValue(thisRef, property, value)
    }

    override fun toString(): String {
        return "DefaultValueSpDelete(saver=$saver)"
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                V>
                AbsSpSaver.Delegate<SpSaver, V?>.defaultValue(
            defaultValue: V & Any
        ): DefaultValueSpDelete<SpSaver, Sp, Ed, V> =
            object : DefaultValueSpDelete<SpSaver, Sp, Ed, V>(this@defaultValue) {
                override val defaultValue: V & Any = defaultValue
            }

        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                V>
                AbsSpSaver.Delegate<SpSaver, V?>.defaultLazyValue(
            defaultValue: () -> V & Any
        ): DefaultValueSpDelete<SpSaver, Sp, Ed, V> =
            object : DefaultValueSpDelete<SpSaver, Sp, Ed, V>(this@defaultLazyValue) {
                override val defaultValue: V & Any by lazy(LazyThreadSafetyMode.NONE, defaultValue)
            }
    }
}
