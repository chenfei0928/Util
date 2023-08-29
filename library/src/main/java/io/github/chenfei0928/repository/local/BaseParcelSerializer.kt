package io.github.chenfei0928.repository.local

import android.os.Parcel
import io.github.chenfei0928.repository.local.ParcelableUtils.obtainToUse
import io.github.chenfei0928.repository.local.ParcelableUtils.useByParcel
import java.io.InputStream
import java.io.OutputStream

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-26 14:18
 */
@Deprecated(message = "系统版本更新后可能会导致更改数据序列化格式，使用其他方式序列化数据")
abstract class BaseParcelSerializer<T> : LocalSerializer<T> {

    override fun write(outputStream: OutputStream, obj: T & Any) {
        outputStream.write(obtainToUse {
            write(obj)
        })
        outputStream.flush()
    }

    override fun read(inputStream: InputStream): T {
        return inputStream.readBytes().useByParcel {
            read()
        }
    }

    override fun copy(obj: T & Any): T & Any {
        val parcel = Parcel.obtain()
        parcel.write(obj)
        parcel.setDataPosition(0) // This is extremely important!
        val copied = parcel.read()
        parcel.recycle()
        return copied
    }

    abstract fun Parcel.write(obj: T & Any)

    abstract fun Parcel.read(): T & Any
}
