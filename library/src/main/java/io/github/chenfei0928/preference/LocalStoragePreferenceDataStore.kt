package io.github.chenfei0928.preference

import io.github.chenfei0928.preference.base.BasePreferenceDataStore
import io.github.chenfei0928.preference.base.FieldAccessor
import io.github.chenfei0928.preference.base.MutableFieldAccessor
import io.github.chenfei0928.repository.local.LocalFileStorage
import io.github.chenfei0928.repository.local.LocalFileStorage0

/**
 * 用于 [LocalFileStorage] 和 [LocalFileStorage0] 的首选项值访问
 *
 * 如果不想要使用 mutable 方式写入字段而是用 copy，在导入包时导入
 * [io.github.chenfei0928.preference.base.DataCopyClassFieldAccessor.Companion.property]
 * 而非 [MutableFieldAccessor.Companion.property]，同时传入 [fieldAccessor]
 * 的将 [MutableFieldAccessor.Impl.redirectToMutableField] 设置为false。
 *
 * @author chenf()
 * @date 2024-12-26 18:30
 */
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class LocalStoragePreferenceDataStore<T : Any>(
    private val storage: Storage<T>,
    private val fieldAccessor: MutableFieldAccessor<T> = MutableFieldAccessor.Impl(
        redirectToMutableField = true, readCache = true
    ),
) : BasePreferenceDataStore<T>(fieldAccessor), MutableFieldAccessor<T> by fieldAccessor {

    /**
     * 如果不想要使用 mutable 方式写入字段而是用 copy，在导入包时导入
     * [io.github.chenfei0928.preference.base.DataCopyClassFieldAccessor.Companion.property]
     * 而非 [MutableFieldAccessor.Companion.property]，同时将 [redirectToMutableField] 设置为false。
     *
     * @param redirectToMutableField 设置为true允许将部分字段引用时尽量使用 mutable 方式写入字段而非 copy 方式
     */
    constructor(
        storage: Storage<T>, redirectToMutableField: Boolean, readCache: Boolean,
    ) : this(storage, MutableFieldAccessor.Impl(redirectToMutableField, readCache))

    override fun <V> FieldAccessor.Field<T, V>.set(value: V) {
        storage.set(setValue(storage.get(), value))
    }

    override fun <V> FieldAccessor.Field<T, V>.get(): V =
        getValue(storage.get())

    interface Storage<T : Any> {
        fun get(): T
        fun set(value: T)
    }
}
