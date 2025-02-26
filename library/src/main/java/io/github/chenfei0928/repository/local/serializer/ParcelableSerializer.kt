package io.github.chenfei0928.repository.local.serializer

import android.os.Build
import android.os.Parcelable
import androidx.annotation.Discouraged
import io.github.chenfei0928.os.ParcelUtil
import io.github.chenfei0928.repository.local.LocalSerializer
import java.io.InputStream
import java.io.OutputStream

/**
 * @author chenf()
 * @date 2025-02-21 16:17
 */
@Discouraged(
    "don't use Parcelable to local storage," +
            " because it can't sense the content version," +
            " recommend use `VersionedSerializer` wrap."
)
class ParcelableSerializer<T : Parcelable>(
    private val creator: Parcelable.Creator<T>,
    override val defaultValue: T,
) : LocalSerializer<T> {

    override fun write(outputStream: OutputStream, obj: T) {
        outputStream.write(ParcelUtil.marshall {
            obj.writeToParcel(it, 0)
        })
        outputStream.flush()
    }

    override fun read(inputStream: InputStream): T {
        val bytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            inputStream.readAllBytes()
        } else {
            inputStream.readBytes()
        }
        return ParcelUtil.unmarshall(bytes) {
            creator.createFromParcel(it)
        }
    }

    override fun copy(obj: T): T {
        return ParcelUtil.copy(obj, creator)
    }

    override fun toString(): String {
        return "ParcelableSerializer<${defaultValue.javaClass.name}>"
    }
}
