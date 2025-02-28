package io.github.chenfei0928.os

import android.os.Parcel
import android.os.Parcelable.Creator
import androidx.core.os.ParcelCompat
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.TypeParceler

/**
 * 使List对象支持Parcelable的序列化支持，需创建子类并传递解析器
 * ```
 *   @TypeParceler<InitReply.SandboxVersion?, SandboxVersionParceler>()
 *   val remoteSandboxVersion: InitReply.SandboxVersion?,
 *
 *   object SandboxVersionParceler : ProtobufListParserParser<SandboxVersion>(SandboxVersion.parser())
 * ```
 * [Docs](https://developer.android.com/kotlin/parcelize?hl=zh-cn)
 *
 * 此类必须abstract，以让使用处创建子类才能被添加到[TypeParceler]注解中使用
 *
 * [Parceler.newArray]不需要重写，由编译器直接在[Creator.createFromParcel]中生成
 *
 * @author chenf()
 * @date 2024-12-12 18:22
 */
open class ListParser<T>(
    private val parceler: Parceler<T>,
) : Parceler<List<T?>?> {

    final override fun create(parcel: Parcel): List<T?>? {
        val size = parcel.readInt()
        return if (size < 0) {
            null
        } else (0 until size).map {
            if (ParcelCompat.readBoolean(parcel)) {
                parceler.create(parcel)
            } else null
        }
    }

    final override fun List<T?>?.write(parcel: Parcel, flags: Int) = if (this == null) {
        parcel.writeInt(-1)
    } else {
        parcel.writeInt(size)
        forEach {
            ParcelCompat.writeBoolean(parcel, it != null)
            parceler.run {
                it?.write(parcel, flags)
            }
        }
    }

    override fun toString(): String {
        return "ListParser(parceler=$parceler)"
    }
}
