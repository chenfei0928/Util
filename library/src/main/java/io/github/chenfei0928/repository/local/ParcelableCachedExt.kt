package io.github.chenfei0928.repository.local

import android.os.BadParcelableException
import android.os.Parcelable
import java.lang.reflect.Modifier

/**
 * [android.os.Parcel.readParcelableCreator]
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-11-24 16:33
 */
val <T : Parcelable> Class<T>.PARCELABLE_CREATOR: Parcelable.Creator<T>
    get() {
        val f = getField("CREATOR")
        if (f.modifiers and Modifier.STATIC == 0) {
            throw BadParcelableException("Parcelable protocol requires "
                    + "the CREATOR object to be static on class " + name)
        }
        val creatorType = f.type
        if (!Parcelable.Creator::class.java.isAssignableFrom(creatorType)) {
            // Fail before calling Field.get(), not after, to avoid initializing
            // parcelableClass unnecessarily.
            throw BadParcelableException("Parcelable protocol requires a "
                    + "Parcelable.Creator object called "
                    + "CREATOR on class " + name)
        }
        return f.get(null) as Parcelable.Creator<T>
    }
