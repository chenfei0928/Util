package io.github.chenfei0928.util

import chenfei0928.normal.MainNoPackageSingleFile
import chenfei0928.out.MainJavaOuter
import com.google.protobuf.Descriptors
import com.google.protobuf.jvmFullyQualifiedName
import com.google.protobuf.parentForTesting
import com.google.protobuf.toShortString
import io.github.chenfei0928.demo.bean.single.MainSingleFile
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        println()
        logProto(
            MainJavaOuter.Test.InnerEnum.getDescriptor(),
            MainJavaOuter.Test.InnerEnum::class.java
        )
        logProto(
            chenfei0928.outmulti.Test.InnerEnum.getDescriptor(),
            chenfei0928.outmulti.Test.InnerEnum::class.java
        )
        logProto(
            MainSingleFile.Test.InnerEnum.getDescriptor(),
            MainSingleFile.Test.InnerEnum::class.java
        )
        logProto(
            MainNoPackageSingleFile.Test.InnerEnum.getDescriptor(),
            MainNoPackageSingleFile.Test.InnerEnum::class.java
        )
        logProto(
            io.github.chenfei0928.demo.bean.Test.InnerEnum.getDescriptor(),
            io.github.chenfei0928.demo.bean.Test.InnerEnum::class.java
        )
        logProto(io.github.chenfei0928.demo.bean.Test.getDescriptor().findFieldByNumber(2))
    }

    fun logProto(des: Descriptors.GenericDescriptor, clazz: Class<*>? = null) {
        println(buildString {
            append("fileName: ")
            append(des.file.name)
            append(", options: ")
            append(des.file.options.toShortString())
        })
        println(buildString {
            append("name: ")
            append(des.name)
            append(", fullName: ")
            append(des.fullName)
            appendLine(", 继承关系树:")
            var parent = des.parentForTesting
            while (parent != null) {
                append(parent.name)
                append(" - ")
                parent = parent.parentForTesting
            }
            appendLine()
            append(clazz?.name)
            appendLine()
            append(des.jvmFullyQualifiedName)
            appendLine()
        })
    }
}
