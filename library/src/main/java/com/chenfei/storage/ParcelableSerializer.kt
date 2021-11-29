package com.chenfei.storage

import android.os.Parcel
import android.os.Parcelable

/**
 * 对[Parcelable]进行序列化和反序列化
 * 由于其API在开始时不是为持久化数据设计，会导致其数据结构定义变化后反序列化时大概率会产生数据混乱的问题
 */
class ParcelableSerializer<T : Parcelable>(
        private val creator: Parcelable.Creator<T>
) : BaseParcelSerializer<T>() {

    constructor(clazz: Class<T>) : this(clazz.PARCELABLE_CREATOR)

    override fun save(parcel: Parcel, obj: T) {
        obj.writeToParcel(parcel, 0)
    }

    override fun load(parcel: Parcel): T? {
        return creator.createFromParcel(parcel)
    }
}
