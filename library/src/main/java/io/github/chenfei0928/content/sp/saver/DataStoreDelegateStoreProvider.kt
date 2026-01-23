package io.github.chenfei0928.content.sp.saver

import android.content.SharedPreferences
import io.github.chenfei0928.content.sp.saver.convert.SpValueObservable
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @param enableFieldObservable 是否启用字段级别的观察者
 *
 * @author chenf()
 * @date 2024-12-23 11:33
 */
class DataStoreDelegateStoreProvider<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        V>
constructor(
    private val enableFieldObservable: Boolean,
    private val findSpAccessorDelegateIfStruct: Boolean,
    private val delegate: AbsSpSaver.Delegate<SpSaver, V>
) : PropertyDelegateProvider<SpSaver, ReadWriteProperty<SpSaver, V>> {

    override fun provideDelegate(
        thisRef: SpSaver, property: KProperty<*>
    ): ReadWriteProperty<SpSaver, V> {
        val delegate: AbsSpSaver.Delegate<SpSaver, V> = if (enableFieldObservable)
            SpValueObservable(delegate) else delegate

        @Suppress("UNCHECKED_CAST")
        val field = thisRef.fieldAccessorCache.property(
            property as KProperty<V>,
            delegate.spValueType as PreferenceType<V>,
            findSpAccessorDelegateIfStruct,
            delegate
        )
        thisRef.fieldAccessorCache.onPropertyAdded(field)
        return delegate
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                V> AbsSpSaver.Delegate<SpSaver, V>.dataStore(
            enableFieldObservable: Boolean,
            findSpAccessorDelegateIfStruct: Boolean,
        ): DataStoreDelegateStoreProvider<SpSaver, Sp, Ed, V> = DataStoreDelegateStoreProvider(
            enableFieldObservable, findSpAccessorDelegateIfStruct, this
        )
    }
}
