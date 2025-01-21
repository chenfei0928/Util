package io.github.chenfei0928.content.sp.saver

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author chenf()
 * @date 2024-12-23 11:33
 */
class DataStoreDelegateStoreProvider<SpSaver : AbsSpSaver<SpSaver>, V>(
    private val delegate: AbsSpSaver.AbsSpDelegate<V>
) : PropertyDelegateProvider<SpSaver, ReadWriteProperty<SpSaver, V>> {

    override fun provideDelegate(
        thisRef: SpSaver, property: KProperty<*>
    ): ReadWriteProperty<SpSaver, V> {
        @Suppress("UNCHECKED_CAST")
        thisRef.dataStore.property(property as KProperty<V>, delegate.spValueType, delegate)
        return delegate
    }

    companion object {
        fun <SpSaver : AbsSpSaver<SpSaver>, V> AbsSpSaver.AbsSpDelegate<V>.dataStore():
                DataStoreDelegateStoreProvider<SpSaver, V> =
            DataStoreDelegateStoreProvider(this)
    }
}
