package io.github.chenfei0928.demo.storage

import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import com.google.protobuf.ProtobufParceler
import io.github.chenfei0928.demo.bean.Test
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

/**
 * @author chenf()
 * @date 2024-11-07 16:56
 */
@Parcelize
data class Bean(
    @Transient
    @TypeParceler<Test, ProtobufParceler.Instance>
    val t: Test = Test.getDefaultInstance(),
    @TypeParceler<Test, ProtobufParceler.Instance>
    val te: Test = Test.getDefaultInstance(),
    val state: Lifecycle.State = Lifecycle.State.INITIALIZED,
) : Parcelable
