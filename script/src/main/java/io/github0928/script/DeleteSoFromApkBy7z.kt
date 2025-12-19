package io.github0928.script

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * @author chenf()
 * @date 2025-12-18 10:28
 */
fun main() {
    val apkFile = File("E:\\Project\\CX835_Digital_dji\\CX835_Trax_base.apk")
    val apkOutFile = File("E:\\Project\\CX835_Digital_dji\\CX835_Trax_base_out.apk")
    val soExcludeList = File("E:\\Project\\CX835_Digital_dji\\app\\so_exclude.txt")
        .readLines().map { it.trim() }
    println(soExcludeList)
    println("open input output stream")
    println()
    val zipOutputStream = ZipOutputStream(apkOutFile.outputStream())
    ZipInputStream(apkFile.inputStream()).forEach {
        if (it.name.trim() in soExcludeList) {
            return@forEach
        }
        zipOutputStream.putNextEntry(it)
        copyTo(zipOutputStream)
    }
    zipOutputStream.close()
}

inline fun ZipInputStream.forEach(block: ZipInputStream.(entry: ZipEntry) -> Unit) {
    var zipEntry = nextEntry
    while (zipEntry != null) {
        block(zipEntry)
        closeEntry()
        zipEntry = nextEntry
    }
}
