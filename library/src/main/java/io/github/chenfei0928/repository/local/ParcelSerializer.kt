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
@Deprecated(message = "系统版本更新后可能会导致更改数据序列化格式，使用其他方式序列化数据")
class ParcelSerializer<T : Any>(
    private val classLoader: ClassLoader = ParcelSerializer::class.java.classLoader!!
) : BaseParcelSerializer<T?>() {

    override val defaultValue: T? = null

    override fun Parcel.write(obj: T) {
        writeValue(obj)
    }

    override fun Parcel.read(): T {
        return readValue(classLoader) as T
    }
}

@Deprecated(message = "系统版本更新后可能会导致更改数据序列化格式，使用其他方式序列化数据")
inline fun <reified T : Any> ParcelSerializer() =
    ParcelSerializer<T>(T::class.java.classLoader!!)
