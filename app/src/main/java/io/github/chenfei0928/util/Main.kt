package io.github.chenfei0928.util

import android.os.Parcelable
import com.google.protobuf.ProtobufLiteParceler
import com.google.protobuf.ProtobufParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

/**
 * @author chenf()
 * @date 2024-07-30 15:14
 */
@Parcelize
data class Main(
    @TypeParceler<Test, ProtobufLiteParceler.Instance>()
    val t: Test = Test.getDefaultInstance(),
    @TypeParceler<Test?, TestParceler>()
    val t1: Test? = Test.getDefaultInstance(),
) : Parcelable

private class TestParceler : ProtobufParceler<Test>(Test.parser())
