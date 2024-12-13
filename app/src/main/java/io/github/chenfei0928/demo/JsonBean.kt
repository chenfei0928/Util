package io.github.chenfei0928.demo

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * @author chenf()
 * @date 2024-12-11 17:25
 */
@Serializable
@Parcelize
data class JsonBean(
    val i: Int = 0,
    @Transient
    @IgnoredOnParcel
    val i1: Int = 1,
) : Parcelable
