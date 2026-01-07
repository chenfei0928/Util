package io.github0928.script

import java.io.File

/**
 * 路径规则：
 *
 * 缓存路径类型（WhiteList、ChromeCacheDir、Dir）|绝对路径（支持环境变量与通配符和<Version>版本号标记）
 *
 * 绝对路径中支持插入环境变量，例如：`"%APPDATA%/Tencent/QQMusic"`。
 * [File.separator] 路径分隔符为正斜杠（/）。
 * 白名单的目录及其子目录均不会删除。
 * 通配符只用来匹配目录名，而非路径（跨目录）。
 * 拥有版本号标记的，将会对前缀一致的路径的进行group并排除版本号最新的目录，即只保留最新的目录。
 *
 * @author chenf()
 * @date 2026-01-07 15:25
 */
fun main(args: Array<String>) {
    println(deEnv("%APPDATA%/Tencent/QQMusic"))
}

private fun deEnv(str: String): String {
    val startIndex = str.indexOf("%")
    if (startIndex == -1) return str
    val endIndex = str.indexOf("%", startIndex + 1)
    val env = System.getenv(str.substring(startIndex + 1, endIndex)).replace("\\", "/")
    return str.take(startIndex) + env + deEnv(str.substring(endIndex + 1))
}

private fun listFileByPattern(path: String): List<File> {
    val indexOfWildcard = path.indexOf('*')
    val indexOfVersion = path.indexOf(VERSION_PLACEHOLDER)
    if (indexOfWildcard == -1 && indexOfVersion == -1) {
        return listOf(File(path))
    }
    if (path.substring(
            minOf(indexOfWildcard, indexOfVersion),
            maxOf(indexOfWildcard, indexOfVersion)
        ).contains('/')
    ) {

    }
}

private const val VERSION_PLACEHOLDER = "<Version>"
