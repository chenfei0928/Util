package io.github.chenfei0928.storage

import android.os.Parcel
import io.github.chenfei0928.storage.ParcelableUtils.obtainToUse
import io.github.chenfei0928.storage.ParcelableUtils.useByParcel
import java.io.InputStream
import java.io.OutputStream

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-26 14:18
 */
abstract class BaseParcelSerializer<T> : LocalSerializer<T> {
    override fun save(outputStream: OutputStream, obj: T) {
        outputStream.write(obtainToUse {
            save(this, obj)
        })
        outputStream.flush()
    }

    override fun load(inputStream: InputStream): T? {
        return inputStream.readBytes().useByParcel {
            load(this)
        }
    }

    override fun copy(obj: T): T {
        val parcel = Parcel.obtain()
        save(parcel, obj)
        parcel.setDataPosition(0) // This is extremely important!
        val copied = load(parcel)
        parcel.recycle()
        return copied!!
    }

    abstract fun save(parcel: Parcel, obj: T)

    abstract fun load(parcel: Parcel): T?
}
