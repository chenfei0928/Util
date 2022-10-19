package io.github.chenfei0928.app.callback

import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import kotlinx.parcelize.Parcelize

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-01-13 16:06
 */
@Parcelize
data class ActivityInfo(
    @JvmField val processName: String,
    @JvmField val packageName: String,
    @JvmField val activityClassName: String,
    @JvmField val instanceHashcode: Int,
    @JvmField val event: Lifecycle.Event,
) : Parcelable
