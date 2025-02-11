package io.github.chenfei0928.content.sp.saver

import android.content.SharedPreferences
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-12-23 11:33
 */
class DataStoreDelegateStoreProvider<
        SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
        Sp : SharedPreferences,
        Ed : SharedPreferences.Editor,
        V>
constructor(
    private val delegate: AbsSpSaver.AbsSpDelegate<SpSaver, V>
) : PropertyDelegateProvider<SpSaver, ReadWriteProperty<SpSaver, V>> {

    override fun provideDelegate(
        thisRef: SpSaver, property: KProperty<*>
    ): ReadWriteProperty<SpSaver, V> {
        @Suppress("UNCHECKED_CAST")
        thisRef.dataStore.property(property as KProperty<V>, delegate.spValueType, delegate)
        return delegate
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver, Sp, Ed>,
                Sp : SharedPreferences,
                Ed : SharedPreferences.Editor,
                V> AbsSpSaver.AbsSpDelegate<SpSaver, V>.dataStore():
                DataStoreDelegateStoreProvider<SpSaver, Sp, Ed, V> =
            DataStoreDelegateStoreProvider(this)
    }
}
