package com.chenfei.module

import android.os.Parcel
import android.os.Parcelable

/**
 * [Github仓库文件](https://github.com/fengzhizi715/SAF/blob/master/saf-cache/src/main/java/com/safframework/cache/ParcelableUtils.java)
 * Created by Tony Shen on 2017/6/20.
 */
object ParcelableUtils {
    /**
     * 创建一个Parcel并使用以写入数据
     */
    private inline fun obtainToUse(block: Parcel.() -> Unit): ByteArray {
        val parcel = Parcel.obtain()
        block(parcel)
        val bytes = parcel.marshall()
        parcel.recycle()
        return bytes
    }

    /**
     * 使用读取的ByteArray创建Parcel并读取其
     */
    private inline fun <T> ByteArray.useByParcel(block: Parcel.() -> T): T {
        val parcel = Parcel.obtain()
        parcel.unmarshall(this, 0, size)
        parcel.setDataPosition(0) // This is extremely important!
        val result = block(parcel)
        parcel.recycle()
        return result
    }

    fun <T : Parcelable> marshall(parcelable: T): ByteArray {
        return obtainToUse {
            parcelable.writeToParcel(this, 0)
        }
    }

    fun <T : Parcelable> unmarshall(bytes: ByteArray, creator: Parcelable.Creator<T>): T {
        return bytes.useByParcel {
            creator.createFromParcel(this)
        }
    }

    fun <T : Parcelable> marshallArray(parcelable: Array<T>): ByteArray {
        return obtainToUse {
            writeTypedArray(parcelable, 0)
        }
    }

    fun <T : Parcelable> unmarshallArray(
        bytes: ByteArray,
        creator: Parcelable.Creator<T>
    ): Array<T>? {
        return bytes.useByParcel {
            createTypedArray(creator)
        }
    }

    fun <T : Parcelable> marshallList(parcelable: List<T>): ByteArray {
        return obtainToUse {
            writeTypedList(parcelable)
        }
    }

    fun <T : Parcelable> unmarshallList(
        bytes: ByteArray,
        creator: Parcelable.Creator<T>
    ): List<T>? {
        return bytes.useByParcel {
            createTypedArrayList(creator)
        }
    }
}
