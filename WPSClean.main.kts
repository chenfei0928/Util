#!/usr/bin/env kotlin

import java.io.File
import java.lang.module.ModuleDescriptor

val appData = System.getenv("APPDATA")
val local = System.getenv("LOCALAPPDATA")
val userHome = System.getenv("USERPROFILE")
arrayOf(
    File(appData, """\kingsoft\wps\addons\pool\win-i386"""),
    File(appData, """\kingsoft\wps\addons\pool\win-x64"""),
).flatMap { it.listFiles()?.toList() ?: emptyList() }.let {
    cleanPoolOldVersion(it)
}

// 缓存与日志目录清理
arrayOf(
    File(appData, "\\LarkShell\\sdk_storage\\log"),
    File(appData, "\\LarkShell\\update"),
    File(appData, "\\Tencent\\Logs"),
    File(appData, "\\aDrive\\logs"),
    File(appData, "\\BaiduYunGuanjia\\logs"),
    File(appData, "\\baidu\\BaiduNetdisk\\AutoUpdate"),
    File(appData, "\\Tencent\\WXWork\\Log"),
    File(appData, "\\Tencent\\WXWork\\upgrade"),
    File(appData, "\\Tencent\\xwechat\\log"),
    File(appData, "\\Tencent\\xwechat\\update"),
    File(appData, "\\Tencent\\WeMeet\\Global\\Logs"),
    File(appData, "\\Tencent\\WeMeet\\Global\\Update"),
    File(appData, "\\Tencent\\Logs"),
    File(appData, "\\heybox-chat-electron\\log"),
    File(appData, "\\BaiduYunKernel\\Data"),
    File(appData, "\\BaiduYunGuanjia\\BaiduYunKernel\\VideoLog"),
    File(appData, "\\HandBrake\\logs"),
    File(appData, "\\Tencent\\TencentVideoMPlayer\\webkit_cache"),

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

    File(userHome, "\\.comate-engine\\log"),
    File(userHome, "\\.comate-engine\\mpc-log"),

    File(System.getenv("TEMP")),
    File(System.getenv("TMP")),
).forEach {
    println("delete: $it")
    it.deleteRecursively()
}

File(userHome, "\\.comate-engine\\bin\\agent").listFiles { it.isDirectory }
    ?.toMutableList()?.let {
        it -= it.maxBy { it.lastModified() }
        it.forEach {
            println("delete: $it")
            it.deleteRecursively()
        }
    }

// Chrome内核缓存清理
arrayOf(
    File(appData, "\\heybox-chat-electron"),
    File(appData, "\\MrRSS.exe\\EBWebView\\Default"),
    File(appData, "\\Code"),
    File(appData, "\\gmm"),
    File(appData, "\\baidunetdisk"),
    File(appData, "\\aDrive\\Partitions\\adrive"),
    File(appData, "\\FLiNGTrainer\\cef-cache"),

    File(local, "\\Yodao\\DeskDict\\dict.cache"),
    File(local, "\\Steam\\htmlcache\\Default"),
    File(local, "\\Ubisoft Game Launcher\\cache\\http2\\Default"),
    File(local, "\\io.github.clash-verge-rev.clash-verge-rev\\EBWebView\\Default"),
    File(local, "\\MI\\XiaomiPCManager\\EBWebView\\Default"),
).forEach {
    cleanChromeCache(it)
}

// Epic Games Launcher 缓存清理
File(local, "\\EpicGamesLauncher\\Saved").listFiles { _, name ->
    name.startsWith("webcache_")
}.forEach {
    cleanChromeCache(it)
}

// 子目录为版本号，版本号下面是 Chrome内核缓存的清理
arrayOf(
    File(appData, "\\Tencent\\WXWork\\Applet"),
    File(appData, """\Tencent\WXWork\WXDrive"""),
    File(appData, """\Tencent\WXWork\WeMailNode"""),
    File(appData, """\Tencent\WXWork\WxWorkDocConvert"""),
    File(appData, """\Tencent\WXWork\WeChatOCR"""),
    File(appData, """\Tencent\WXWork\FlutterPlugins"""),

    File(local, "\\Battle.net\\BrowserCaches"),
).forEach {
    val files = it.listFiles()?.filter { it.isDirectory }
    files?.forEach { file ->
        cleanChromeCache(file)
    }
}

// Figma 缓存清理，其版本信息有前缀字符 v
File(appData, "\\Figma\\DesktopProfile").listFiles { it.isDirectory }?.toList()?.let {
    (it - it.maxBy { ModuleDescriptor.Version.parse(it.name.substring(1)) })?.forEach {
        println("delete: $it")
        it.deleteRecursively()
    }
}

// 子目录为版本号的目录清理
arrayOf(
    File(appData, "\\Tencent\\WXWork\\wmpf_Applet"),
    File(appData, """\Tencent\WXWork\WXDrive_x64"""),
    File(appData, """\Tencent\WXWork\WeMailNode_x64"""),
    File(appData, """\Tencent\WXWork\WxWorkDocConvert"""),
    File(appData, """\Tencent\WXWork\WeChatOCR"""),
    File(appData, """\Tencent\WXWork\FlutterPlugins"""),
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
    val files = dir.listFiles { it.isDirectory }?.toList()
        ?: return
    (files - files.maxBy { ModuleDescriptor.Version.parse(it.name) }).forEach {
        println("delete: $it")
        it.deleteRecursively()
    }
}

// 子目录为插件目录，再往下是版本号的目录清理
arrayOf(
    File(appData, "\\Tencent\\xwechat\\XPlugin\\plugins"),
    File(appData, "\\Tencent\\xwechat\\radium\\Applet\\packages"),
).forEach {
    it.listFiles()?.forEach {
        cleanVersionNameDirs(it)
    }
}

// 飞书缓存清理
File(appData, "\\LarkShell\\iron\\users").listFiles { it.isDirectory }
    ?.forEach { cleanChromeCache(File(it, "profile_main")) }

val larkShellGlobal = File(appData, "\\LarkShell\\aha\\users\\global")
cleanChromeCache(File(larkShellGlobal, "profile_global"))
File(appData, "\\LarkShell\\aha\\users")
    .listFiles { _, name -> name != "global" }
    ?.forEach { it ->
        cleanChromeCache(File(it, "profile_explorer"))
        cleanChromeCache(File(it, "profile_main"))
    }
File(appData, "\\LarkShell\\PC_Gadget").listFiles()?.forEach {
    File(it, "app").listFiles { _, name -> name.startsWith("cli_") }.forEach { it ->
        cleanVersionNameDirs(it)
    }
    File(it, "__p_block__\\app").listFiles { _, name -> name.startsWith("blk_") }.forEach { it ->
        cleanVersionNameDirs(it)
    }
}

private fun cleanChromeCache(dir: File) {
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
