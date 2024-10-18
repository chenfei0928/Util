package io.github.chenfei0928.compiler

import com.android.build.gradle.AppExtension
import io.github.chenfei0928.Contract
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.checkApp
import io.github.chenfei0928.util.child
import io.github.chenfei0928.util.tmpProguardFilesDir
import io.github.chenfei0928.util.writeTmpProguardFile
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.task
import java.util.Locale
import java.util.Random

/**
 * 应用自定义混淆规则，生成混淆字典
 *
 * 沙盒混淆
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-10-26 10:19
 */
fun Project.applyAppDictObfuscation() {
    checkApp("applyAppDictObfuscation")

    // 有混淆保持文件则不生成混淆字典
    if (containsMappingKeepingFile) {
        return
    }
    // 混淆字典规则表
    val obfuscationDictProguardFile = writeTmpProguardFile(
        "obfuscation_dict.pro", obfuscationDictProguardContent
    )
    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        defaultConfig {
            proguardFile(obfuscationDictProguardFile)
        }
    }
    // 创建生成混淆字典的任务
    val genObfuscationDict = task<GenObfuscationDictTask>(
        "genObfuscationDict"
    ) {
        outputs.file(tmpProguardFilesDir.child {
            obfuscationDictFileName
        })
    }

    // 将混淆字典生成任务添加到编译任务的依赖中
    afterEvaluate {
        buildSrcAndroid<AppExtension> {
            applicationVariants.all {
                val preBuildName = getPreBuildName(name)
                    ?: return@all
                val task = tasks.getByName(preBuildName)
                task.dependsOn(genObfuscationDict)
            }
        }
    }
}

private abstract class GenObfuscationDictTask : DefaultTask() {
    @TaskAction
    fun genObfuscationDict() {
        val f: java.io.File = outputs.files.singleFile
        if (f.exists()) {
            return
        }

        val r = Random()
        val start = r.nextInt(1000) + 0x0100
        val end = start + 0x4000
        val chars = (start..end)
            .filter { Character.isValidCodePoint(it) && Character.isJavaIdentifierPart(it) }
            .map { java.lang.String.valueOf(Character.toChars(it)) }
            .toMutableList()
        val startChars = mutableListOf<String>()
        val dict = mutableListOf<String>()
        // 筛选可用作java标识符开头的char
        chars.forEach { str ->
            val c = str.first()
            if (Character.isJavaIdentifierStart(c)) {
                startChars.add(java.lang.String.valueOf(c))
            }
        }
        val startSize = startChars.size
        // 每次build都打乱顺序
        chars.shuffle(r)
        startChars.shuffle(r)
        // 拼两个char为一个词，让字典更丰富
        chars.forEach { c ->
            val m = r.nextInt(startSize - 3)
            val n = m + 3
            for (j in m..n) {
                dict.add(startChars[j] + c)
            }
        }

        outputs.files.singleFile.writer().use {
            it.write(startChars.joinToString(System.lineSeparator()))
            it.write(dict.joinToString(System.lineSeparator()))
        }
    }
}

private const val obfuscationDictFileName = "obfuscation_dict.txt"
private const val obfuscationDictProguardContent = """
# 由buildSrc中Obfuscation.kt文件自动生成
# 此路径是相对于该pro文件的路径
-obfuscationdictionary $obfuscationDictFileName
-classobfuscationdictionary $obfuscationDictFileName
-packageobfuscationdictionary $obfuscationDictFileName
"""

private fun getPreBuildName(variantName: String): String? {
    if (variantName == "release") {
        return "preReleaseBuild"
    }
    Contract.minifyBuildTypes.find { variantName.endsWith(it, true) }?.let {
        val upcaseVariantName = variantName.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
        return "pre" + upcaseVariantName + "Build"
    }
    return null
}
