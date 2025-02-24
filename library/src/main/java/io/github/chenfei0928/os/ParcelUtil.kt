package io.github.chenfei0928.os

import android.os.Parcel
import android.os.Parcelable

/**
 * [Github仓库文件](https://github.com/fengzhizi715/SAF/blob/master/saf-cache/src/main/java/com/safframework/cache/ParcelableUtils.java)
 * 基于它，并有所扩展
 *
 * Created by Tony Shen on 2017/6/20.
 */
object ParcelUtil {
    inline fun <T> use(
        block: (Parcel) -> T
    ): T {
        val parcel = Parcel.obtain()
        return try {
            block(parcel)
        } finally {
            parcel.recycle()
        }
    }

    /**
     * 创建一个Parcel并使用以写入数据
     */
    inline fun marshall(
        block: (Parcel) -> Unit
    ): ByteArray = use {
        block(it)
        it.marshall()
    }

    /**
     * 使用读取的ByteArray创建Parcel并读取其
     */
    inline fun <T> unmarshall(
        bytes: ByteArray, block: (Parcel) -> T
    ): T = use {
        it.unmarshall(bytes, 0, bytes.size)
        it.setDataPosition(0) // This is extremely important!
        block(it)
    }

    fun <T : Parcelable> marshall(
        parcelable: T
    ): ByteArray = marshall {
        parcelable.writeToParcel(it, 0)
    }

    fun <T : Parcelable> unmarshall(
        bytes: ByteArray, creator: Parcelable.Creator<T>
    ): T = unmarshall(bytes) {
        creator.createFromParcel(it)
    }

    fun <T : Parcelable> marshallArray(
        parcelable: Array<T>
    ): ByteArray = marshall {
        it.writeTypedArray(parcelable, 0)
    }

    fun <T : Parcelable> unmarshallArray(
        bytes: ByteArray, creator: Parcelable.Creator<T>
    ): Array<T>? = unmarshall(bytes) {
        it.createTypedArray(creator)
    }

    fun <T : Parcelable> marshallList(
        parcelable: List<T>
    ): ByteArray = marshall {
        it.writeTypedList(parcelable)
    }

    fun <T : Parcelable> unmarshallList(
        bytes: ByteArray, creator: Parcelable.Creator<T>
    ): List<T>? = unmarshall(bytes) {
        it.createTypedArrayList(creator)
    }
}
