package io.github.chenfei0928.util.kotlin

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.collection.ArrayMap
import com.google.protobuf.GeneratedMessageLite
import com.google.protobuf.getParseFrom

fun Intent.putParcelableListExtra(name: String, value: List<Parcelable>?): Intent {
    return putParcelableArrayListExtra(name, value?.asArrayList())
}

fun Bundle.putParcelableList(name: String, value: List<Parcelable>?) {
    putParcelableArrayList(name, value?.asArrayList())
}

fun Intent.putExtra(name: String, value: com.google.protobuf.GeneratedMessageLite<*, *>) {
    putExtra(name, value.toByteArray())
}

fun <T : com.google.protobuf.GeneratedMessageLite<*, *>> Intent.getExtra(
    name: String, parse: (ByteArray) -> T
): T? {
    return getByteArrayExtra(name)?.let(parse)
}

inline fun <reified T : GeneratedMessageLite<T, *>, Source> Intent.getExtra(
    name: String
): T? {
    val parseFrom = T::class.java.getParseFrom<T, ByteArray>()
    return getByteArrayExtra(name)?.let(parseFrom)
}

fun Intent.getAllExtras() = extras?.getAll() ?: emptyMap()

/**
 * 同步intent目标包名，仅用与便于在使用[Intent.URI_ANDROID_APP_SCHEME]
 * 进行序列化[Intent.toUri]输出时不用额外再进行指定包名
 *
 * 鬼知道为什么在指定了组件之后不会自动指定intent包名¯\_(ツ)_/¯
 */
fun Intent.syncPackage(): Intent = apply {
    component?.let {
        setPackage(it.packageName)
    }
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
    if (size() != other.size()) {
        return false
    }
    keySet().forEach {
        if (!other.containsKey(it)) {
            return false
        }
        val value = get(it)
        val otherValue = other.get(it)

        if (value != null || otherValue != null) {
            if (!value.deepEquals(otherValue)) {
                return false
            }
        }
    }
    return true
}
