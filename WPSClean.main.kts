#!/usr/bin/env kotlin

import java.io.File
import java.lang.module.ModuleDescriptor

val appDataRoaming = System.getenv("APPDATA")
val local = System.getenv("LOCALAPPDATA")
val userHome = System.getenv("USERPROFILE")
arrayOf(
    File(appDataRoaming, """\kingsoft\wps\addons\pool\win-i386"""),
    File(appDataRoaming, """\kingsoft\wps\addons\pool\win-x64"""),
).flatMap { it.listFiles()?.toList() ?: emptyList() }.let {
    cleanPoolOldVersion(it)
}

// 缓存与日志目录清理
arrayOf(
    File(appDataRoaming, "\\LarkShell\\sdk_storage\\log"),
    File(appDataRoaming, "\\LarkShell\\update"),
    File(appDataRoaming, "\\Tencent\\Logs"),
    File(appDataRoaming, "\\aDrive\\logs"),
    File(appDataRoaming, "\\BaiduYunGuanjia\\logs"),
    File(appDataRoaming, "\\baidu\\BaiduNetdisk\\AutoUpdate"),
    File(appDataRoaming, "\\Tencent\\WXWork\\Log"),
    File(appDataRoaming, "\\Tencent\\WXWork\\upgrade"),
    File(appDataRoaming, "\\Tencent\\xwechat\\log"),
    File(appDataRoaming, "\\Tencent\\xwechat\\update"),
    File(appDataRoaming, "\\Tencent\\WeMeet\\Global\\Logs"),
    File(appDataRoaming, "\\Tencent\\WeMeet\\Global\\Update"),
    File(appDataRoaming, "\\Tencent\\Logs"),
    File(appDataRoaming, "\\heybox-chat-electron\\log"),
    File(appDataRoaming, "\\BaiduYunKernel\\Data"),
    File(appDataRoaming, "\\BaiduYunGuanjia\\BaiduYunKernel\\VideoLog"),
    File(appDataRoaming, "\\HandBrake\\logs"),
    File(appDataRoaming, "\\Tencent\\TencentVideoMPlayer\\webkit_cache"),

    File(local, "\\Microsoft\\OneDrive\\logs"),
    File(local, "\\Figma\\packages"),
    File(local, "\\JetBrains\\IdeaIC2025.2\\log"),
    File(local, "\\EpicGamesLauncher\\Saved\\Logs"),
    File(local, "\\Battle.net\\Cache"),
    File(local, "\\UniGetUI\\Chocolatey\\logs"),
    File(local, "\\ToDesk\\Logs"),
    File(local, "\\sso_cef_cache"),
    File(local, "\\cbox\\cache"),
    File(local, "\\Qingfeng\\HeyboxChat\\log"),
    File(local, "\\Feishu\\app.todelete"),

    File(userHome, "\\.comate-engine\\log"),
    File(userHome, "\\.comate-engine\\mpc-log"),

    File(System.getenv("TEMP")),
    File(System.getenv("TMP")),
).forEach {
    if (it.exists() && it.isDirectory) {
        println("delete: $it")
        it.deleteRecursively()
    }
}

// 飞书目录旧版本
arrayOf(
    File(local, "\\Feishu"),
).forEach {
    it.listFiles {
        it.isDirectory && try {
            ModuleDescriptor.Version.parse(it.name)
            true
        } catch (e: Exception) {
            false
        }
    }?.toList()?.forEach {
        println("delete: $it")
        it.deleteRecursively()
    }
}

File(userHome, "\\.comate-engine\\bin\\agent").listFiles { it.isDirectory }
    ?.toMutableList()?.let {
        it -= it.maxBy { it.lastModified() }
        it.forEach {
            println("delete: $it")
            it.deleteRecursively()
        }
    }

// Chromium内核缓存清理
arrayOf(
    File(appDataRoaming, "\\heybox-chat-electron"),
    File(appDataRoaming, "\\MrRSS.exe\\EBWebView\\Default"),
    File(appDataRoaming, "\\Code"),
    File(appDataRoaming, "\\gmm"),
    File(appDataRoaming, "\\baidunetdisk"),
    File(appDataRoaming, "\\aDrive\\Partitions\\adrive"),
    File(appDataRoaming, "\\FLiNGTrainer\\cef-cache"),

    File(local, "\\Yodao\\DeskDict\\dict.cache"),
    File(local, "\\Steam\\htmlcache\\Default"),
    File(local, "\\Ubisoft Game Launcher\\cache\\http2\\Default"),
    File(local, "\\io.github.clash-verge-rev.clash-verge-rev\\EBWebView\\Default"),
    File(local, "\\MI\\XiaomiPCManager\\EBWebView\\Default"),
).forEach {
    cleanChromiumCache(it)
}

// Epic Games Launcher 缓存清理
File(local, "\\EpicGamesLauncher\\Saved").listFiles { _, name ->
    name.startsWith("webcache_")
}.forEach {
    cleanChromiumCache(it)
}

// 子目录为版本号，版本号下面是 Chromium内核缓存的清理
arrayOf(
    File(appDataRoaming, "\\Tencent\\WXWork\\Applet"),
    File(appDataRoaming, """\Tencent\WXWork\WXDrive"""),
    File(appDataRoaming, """\Tencent\WXWork\WeMailNode"""),
    File(appDataRoaming, """\Tencent\WXWork\WxWorkDocConvert"""),
    File(appDataRoaming, """\Tencent\WXWork\WeChatOCR"""),
    File(appDataRoaming, """\Tencent\WXWork\FlutterPlugins"""),

    File(local, "\\Battle.net\\BrowserCaches"),
).forEach {
    val files = it.listFiles()?.filter { it.isDirectory }
    files?.forEach { file ->
        cleanChromiumCache(file)
    }
}

// Figma 缓存清理，其版本信息有前缀字符 v
File(appDataRoaming, "\\Figma\\DesktopProfile").listFiles { it.isDirectory }?.toList()?.let {
    (it - it.maxBy { ModuleDescriptor.Version.parse(it.name.substring(1)) })?.forEach {
        println("delete: $it")
        it.deleteRecursively()
    }
}

// 子目录为版本号的目录清理
arrayOf(
    File(local, "\\kingsoft\\WPS Office"),
    File(local, "\\youdao\\dict\\Application"),

    File(appDataRoaming, "\\Tencent\\WXWork\\wmpf_Applet"),
    File(appDataRoaming, """\Tencent\WXWork\WXDrive_x64"""),
    File(appDataRoaming, """\Tencent\WXWork\WeMailNode_x64"""),
    File(appDataRoaming, """\Tencent\WXWork\WxWorkDocConvert"""),
    File(appDataRoaming, """\Tencent\WXWork\WeChatOCR"""),
    File(appDataRoaming, """\Tencent\WXWork\FlutterPlugins"""),
).forEach {
    cleanVersionNameDirs(it)
}

private fun cleanVersionNameDirs(dir: File) {
    dir.listFiles()?.forEach { file ->
        if (file.isDirectory && file.listFiles().isNullOrEmpty()) {
            file.deleteRecursively()
            return@forEach
        }
    }
    val files = dir.listFiles { it.isDirectory }?.mapNotNull {
        try {
            ModuleDescriptor.Version.parse(it.name) to it
        } catch (e: Exception) {
            null
        }
    } ?: return
    (files - files.maxBy { it.first }).forEach {
        println("delete: $it")
        it.second.deleteRecursively()
    }
}

// 子目录为插件目录，再往下是版本号的目录清理
arrayOf(
    File(appDataRoaming, "\\Tencent\\xwechat\\XPlugin\\plugins"),
    File(appDataRoaming, "\\Tencent\\xwechat\\radium\\Applet\\packages"),
).forEach {
    it.listFiles()?.forEach {
        cleanVersionNameDirs(it)
    }
}

// 飞书缓存清理
File(appDataRoaming, "\\LarkShell\\iron\\users").listFiles { it.isDirectory }
    ?.forEach { cleanChromiumCache(File(it, "profile_main")) }

val larkShellGlobal = File(appDataRoaming, "\\LarkShell\\aha\\users\\global")
cleanChromiumCache(File(larkShellGlobal, "profile_global"))
File(appDataRoaming, "\\LarkShell\\aha\\users")
    .listFiles { _, name -> name != "global" }
    ?.forEach { it ->
        cleanChromiumCache(File(it, "profile_explorer"))
        cleanChromiumCache(File(it, "profile_main"))
    }
File(appDataRoaming, "\\LarkShell\\PC_Gadget").listFiles()?.forEach {
    File(it, "app").listFiles { _, name -> name.startsWith("cli_") }.forEach { it ->
        cleanVersionNameDirs(it)
    }
    File(it, "__p_block__\\app").listFiles { _, name -> name.startsWith("blk_") }.forEach { it ->
        cleanVersionNameDirs(it)
    }
}

private fun cleanChromiumCache(dir: File) {
    if (!dir.exists()) return
    println("clean Chrome cache: $dir")
    File(dir, "Cache").deleteRecursively()
}

private fun cleanPoolOldVersion(files: List<File>) {
    val namesFile: MutableMap<String, MutableMap<ModuleDescriptor.Version, File>> = HashMap()
    files.forEach { file ->
        if (file.listFiles().isNullOrEmpty()) {
            file.deleteRecursively()
            return@forEach
        }
        val fileName = file.name
        if (!fileName.contains("_")) return@forEach
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
        }
    }
}
