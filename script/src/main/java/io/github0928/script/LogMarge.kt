package io.github0928.script

import java.io.File

/**
 * @author chenf()
 * @date 2025-11-21 11:19
 */
fun main(args: Array<String>) {
    val logDir = File(args.getOrNull(0) ?: "E:\\log")
    val lastLogFile = File(logDir, "logcat.log")
    val files = logDir.listFiles()?.filter {
        it.name != lastLogFile.name && it.name.startsWith("logcat.log")
    }
    File(logDir, "out.log").bufferedWriter().use { out ->
        files?.forEach {
            println(it.name)
            it.bufferedReader().copyTo(out)
            out.flush()
        }
        lastLogFile.bufferedReader().use {
            it.copyTo(out)
        }
        out.flush()
    }
}
