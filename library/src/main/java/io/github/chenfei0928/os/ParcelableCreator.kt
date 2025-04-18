package io.github.chenfei0928.os

import android.os.BadParcelableException
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import io.github.chenfei0928.util.MapCache
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * [android.os.Parcel.readParcelableCreator]
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-24 16:33
 */
@Suppress("UNCHECKED_CAST", "kotlin:S6531")
val <T : Parcelable> Class<T>.PARCELABLE_CREATOR: Parcelable.Creator<T>
    get() = parcelableCache[this] as Parcelable.Creator<T>

private val parcelableCache =
    MapCache.Basic<Class<out Parcelable>, Parcelable.Creator<out Parcelable>> {
        readParcelableCreator(it)
    }

private val parcelReadParcelableCreator: Method by lazy(LazyThreadSafetyMode.NONE) {
    Parcel::class.java.getDeclaredMethod(
        "readParcelableCreator", ClassLoader::class.java
    ).apply {
        isAccessible = true
    }
}

private fun <T : Parcelable> readParcelableCreator(clazz: Class<T>): Parcelable.Creator<T> {
    val creator: Parcelable.Creator<T>? = ParcelUtil.use {
        it.writeString(clazz.name)
        it.setDataPosition(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ParcelCompat.readParcelableCreator(it, clazz.classLoader, clazz)
        } else try {
            val creator = parcelReadParcelableCreator.invoke(it, clazz.classLoader)
            @Suppress("UNCHECKED_CAST")
            creator as Parcelable.Creator<T>?
        } catch (_: ReflectiveOperationException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }
    if (creator != null) {
        return creator
    }
    val f = clazz.getField("CREATOR")
    if (f.modifiers and Modifier.STATIC == 0) {
        throw BadParcelableException(
            "Parcelable protocol requires " + "the CREATOR object to be static on class " + clazz.name
        )
    }
    val creatorType = f.type
    if (!Parcelable.Creator::class.java.isAssignableFrom(creatorType)) {
        // Fail before calling Field.get(), not after, to avoid initializing
        // parcelableClass unnecessarily.
        throw BadParcelableException(
            "Parcelable protocol requires a " + "Parcelable.Creator object called " + "CREATOR on class " + clazz.name
        )
    }
    @Suppress("UNCHECKED_CAST", "kotlin:S6518")
    return f.get(null) as Parcelable.Creator<T>
}
