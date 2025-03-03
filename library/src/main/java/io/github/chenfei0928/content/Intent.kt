package io.github.chenfei0928.content

import android.content.Intent
import android.os.Bundle
import androidx.annotation.ReturnThis
import androidx.collection.ArrayMap
import com.google.protobuf.MessageLite
import com.google.protobuf.protobufParserForType
import io.github.chenfei0928.base.UtilInitializer
import io.github.chenfei0928.lang.deepEquals
import io.github.chenfei0928.os.ParcelUtil
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun Intent.putExtra(name: String, value: MessageLite) {
    putExtra(name, value.toByteArray())
}

inline fun <T : MessageLite> Intent.getProtobufExtra(
    name: String, parse: (ByteArray) -> T
): T? {
    return getByteArrayExtra(name)?.let(parse)
}

inline fun <reified T : MessageLite> Intent.getProtobufExtra(name: String): T? {
    return getByteArrayExtra(name)?.let(T::class.java.protobufParserForType::parseFrom)
}

fun Intent.getAllExtras(): Map<String, Any> = extras?.getAll() ?: emptyMap()

/**
 * 同步intent目标包名，仅用与便于在使用[Intent.URI_ANDROID_APP_SCHEME]
 * 进行序列化[Intent.toUri]输出时不用额外再进行指定包名
 *
 * 鬼知道为什么在指定了组件之后不会自动指定intent包名¯\_(ツ)_/¯
 */
@ReturnThis
fun Intent.syncPackage(): Intent {
    component?.let {
        setPackage(it.packageName)
    }
    return this
}

fun Bundle.getAll(): Map<String, Any> {
    val keySet = keySet()
    val output = ArrayMap<String, Any>(keySet.size)
    keySet.forEach {
        output[it] = this.get(it)
    }
    return output
}

fun Bundle.contentEquals(other: Bundle): Boolean {
    return if (size() != other.size()) {
        return false
    } else keySet().all {
        other.containsKey(it) && get(it).deepEquals(other.get(it))
    }
}

@ReturnThis
fun Intent.zipExtras(): Intent {
    val bundle = extras
        ?: return this
    val zipped = ByteArrayOutputStream().also { out ->
        GZIPOutputStream(out).use {
            it.write(ParcelUtil.marshall(bundle))
        }
    }.toByteArray()
    bundle.keySet().forEach {
        removeExtra(it)
    }
    putExtra(ZIPPED_EXTRA_KEY, zipped)
    return this
}

fun Intent.unzipExtras(classLoader: ClassLoader = UtilInitializer.context.classLoader): Bundle {
    val zipped = getByteArrayExtra(ZIPPED_EXTRA_KEY)
        ?: return Bundle.EMPTY
    val unzipped = GZIPInputStream(ByteArrayInputStream(zipped)).use {
        it.readBytes()
    }
    return ParcelUtil.unmarshall(unzipped, Bundle.CREATOR).apply {
        this.classLoader = classLoader
    }
}

private const val ZIPPED_EXTRA_KEY = "zipped"
