package io.github0928.script

import java.io.File
import java.lang.module.ModuleDescriptor

/**
 * @author chenf()
 * @date 2024-10-18 18:06
 */
class WPSCleanKt {
    fun main() {
        val namesFile: MutableMap<String, MutableMap<ModuleDescriptor.Version, File>> = HashMap()
        File(
            System.getenv("APPDATA"), """\kingsoft\wps\addons\pool\win-i386"""
        ).listFiles()?.forEach { file ->
            val fileName = file.name
            val split = fileName.split("_")
            val name = if (split.size == 2) {
                split[0]
            } else {
                fileName.substringBefore(split[split.size - 1])
            }
            val version = ModuleDescriptor.Version.parse(split.last())
            namesFile.getOrPut(name, ::HashMap)[version] = file
        }
        namesFile.values.filter { it.size > 1 }.forEach { versionFileMap ->
            val maxVersion = versionFileMap.keys.max()
            versionFileMap.remove(maxVersion)
            versionFileMap.values.forEach { file ->
                println("delete: $file")
                file.deleteRecursively()
                deleteDir(file)
            }
        }
    }

    private fun deleteDir(dir: File) {
        if (dir.isFile) {
            dir.delete()
        } else {
            for (file in dir.listFiles()) {
                deleteDir(file)
            }
            dir.delete()
        }
    }
}
