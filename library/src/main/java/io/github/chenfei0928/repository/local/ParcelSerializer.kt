package io.github.chenfei0928.repository.local

import android.os.Parcel
import android.os.Parcelable

/**
 * 使用[Parcel]进行序列化和反序列化，支持其所有自带数据类型（见[Parcel.writeValue]）
 *
 * 在序列化[Parcelable]元素时，[Parcelable]API在开始时不是为持久化数据设计，
 * 在其数据结构定义变化后反序列化时会产生数据混乱的问题
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2022-01-13 18:35
 */
class ParcelSerializer<T>(
    private val classLoader: ClassLoader = ParcelSerializer::class.java.classLoader!!
) : BaseParcelSerializer<T>() {

    override fun Parcel.write(obj: T) {
        writeValue(obj)
    }

    override fun Parcel.read(): T? {
        return readValue(classLoader) as T?
    }
}
