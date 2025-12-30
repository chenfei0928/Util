#!/usr/bin/env kotlin

import java.io.File

val isWindows: Boolean = System.getProperty("os.name").startsWith("Windows")
val gradleHome = System.getenv("GRADLE_USER_HOME") ?: if (isWindows) {
    System.getenv("USERPROFILE") + File.separator + ".gradle"
} else {
    System.getenv("HOME") + File.separator + ".gradle"
}
val gradleCachesDir = File(gradleHome + File.separator + "caches")
val gradleVersionRegex = Regex("\\d+(\\.\\d+)+")
gradleCachesDir.listFiles().filter {
    it.isDirectory && it.name.matches(gradleVersionRegex)
}.forEach {
    val cacheMap = HashMap<Long, ArrayList<File>>()
    File(it, "transforms").listFiles()?.filter {
        it.isDirectory
    }?.forEach {
        if (!deleteIfNoneTransformed(it)) {
            cacheMap.getOrPut(getFileOrDirSize(it), ::ArrayList).add(it)
        }
    }
    println("${it.name} count result: ${cacheMap.values.map { it.size }}")
    cacheMap.values.forEach {
        it -= it.maxBy { it.lastModified() }
        it.forEach { it.deleteRecursively() }
    }
}

fun deleteIfNoneTransformed(file: File): Boolean {
    if (!File(file, "metadata.bin").exists() || !File(file, "results.bin").exists()) {
        return file.deleteRecursively()
    }
    fun File.countFiles(): Int {
        if (isFile) return 1
        val list = listFiles()
        if (list.isNullOrEmpty()) {
            return 0
        }
        return list.sumOf { it.countFiles() }
    }
    return if (File(file, "transformed").countFiles() == 0) {
        file.deleteRecursively()
    } else false
}

/**
 * 获取一个文件或目录的大小
 *
 * @param file 要获取文件尺寸的文件或目录
 * @return 文件尺寸，以字节为单位
 */
fun getFileOrDirSize(
    file: File?
): Long = if (file == null || !file.exists()) {
    // 文件不存在，返回0
    0
} else if (!file.isDirectory) {
    // 路径不是文件夹，返回文件大小
    file.length()
} else {
    // 迭代子文件，并统计总大小
    file.listFiles()
        ?.sumOf { getFileOrDirSize(it) }
        ?: 0
}
