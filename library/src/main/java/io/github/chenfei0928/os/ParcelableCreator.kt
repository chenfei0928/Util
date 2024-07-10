package io.github.chenfei0928.os

import android.os.BadParcelableException
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import io.github.chenfei0928.util.WeakCache
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * [android.os.Parcel.readParcelableCreator]
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-24 16:33
 */
val <T : Parcelable> Class<T>.PARCELABLE_CREATOR: Parcelable.Creator<T>
    get() = parcelableCache[this as Class<Parcelable>] as Parcelable.Creator<T>

private val parcelableCache = WeakCache<Class<*>, Parcelable.Creator<*>> {
    getParcelableCreator(it as Class<Parcelable>)
}

private val parcelReadParcelableCreator: Method by lazy(LazyThreadSafetyMode.NONE) {
    Parcel::class.java.getDeclaredMethod(
        "readParcelableCreator", ClassLoader::class.java
    ).apply {
        isAccessible = true
    }
}

private fun <T : Parcelable> getParcelableCreator(clazz: Class<T>): Parcelable.Creator<T> {
    val creator: Parcelable.Creator<T>? = ParcelUtil.use {
        writeString(clazz.name)
        setDataPosition(0)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                readParcelableCreator(clazz.classLoader, clazz)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                readParcelableCreator(clazz.classLoader) as Parcelable.Creator<T>?
            } else {
                val creator = parcelReadParcelableCreator.invoke(this, clazz.classLoader)
                creator as Parcelable.Creator<T>?
            }
        } catch (e: BadParcelableException) {
            null
        } catch (e: ReflectiveOperationException) {
            null
        } catch (e: SecurityException) {
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
    return f.get(null) as Parcelable.Creator<T>
}
