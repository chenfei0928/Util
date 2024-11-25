import java.io.File
import java.lang.module.ModuleDescriptor

val appData = System.getenv("APPDATA")
arrayOf(
    File(appData, """\kingsoft\wps\addons\pool\win-i386"""),
    File(appData, """\kingsoft\wps\addons\pool\win-x64"""),
).forEach { pool ->
    if (!pool.exists()) {
        return@forEach
    }
    pool.listFiles()?.let {
        cleanPoolOldVersion(it)
    }
}

private fun cleanPoolOldVersion(files: Array<File>) {
    val namesFile: MutableMap<String, MutableMap<ModuleDescriptor.Version, File>> = HashMap()
    files.forEach { file ->
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
