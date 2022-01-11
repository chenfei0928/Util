package io.github.chenfei0928.repository.local

import android.os.Parcel
import android.os.Parcelable

/**
 * 对[Parcelable]数组进行序列化和反序列化
 * 由于其API在开始时不是为持久化数据设计，会导致其数据结构定义变化后反序列化时大概率会产生数据混乱的问题
 */
class ParcelableArraySerializer<T : Parcelable>(
    private val creator: Parcelable.Creator<T>
) : BaseParcelSerializer<Array<T>>() {

    constructor(clazz: Class<T>) : this(clazz.PARCELABLE_CREATOR)

    override fun save(parcel: Parcel, obj: Array<T>) {
        parcel.writeTypedArray(obj, 0)
    }

    override fun load(parcel: Parcel): Array<T>? {
        return parcel.createTypedArray(creator)
    }
}
