package io.github.chenfei0928.demo

import android.os.Parcelable
import com.google.protobuf.ProtobufLiteParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

/**
 * @author chenf()
 * @date 2024-11-07 16:56
 */
@Parcelize
data class Bean(
    @TypeParceler<Test, ProtobufLiteParceler.Instance>
    val t: Test
) : Parcelable
