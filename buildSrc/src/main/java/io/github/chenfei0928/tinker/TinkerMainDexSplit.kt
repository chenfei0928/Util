package io.github.chenfei0928.tinker

import com.android.build.gradle.api.ApplicationVariant
import io.github.chenfei0928.Contract
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.child
import org.gradle.api.Project
import org.gradle.api.Task
import java.util.Locale

/**
 * 修复应用Tinker后MultiDex不生效问题
 *
 * https://github.com/Tencent/tinker/issues/1310
 *
 * https://github.com/Tencent/tinker/issues/1458
 *
 * https://juejin.cn/post/6844904164540022792
 *
 * @author chenf()
 * @date 2022-01-26 16:47
 */
internal fun Project.applyTinkerMainDexSplit() {
    val android = buildSrcAndroid<com.android.build.gradle.AppExtension>()

    afterEvaluate {
        println("handle main-dex by user，start...")
        if (android.defaultConfig.minSdkVersion!!.apiLevel >= 21) {
            return@afterEvaluate
        }
        println("main-dex，minSdkVersion is ${android.defaultConfig.minSdkVersion!!.apiLevel}")
        android.applicationVariants.forEach { variant ->
            val variantName = variant.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            }
            val multidexTask = project.tasks.findByName(
                "transformClassesWithMultidexlistFor${variantName}"
            )
            val exist = multidexTask != null
            println("main-dex multidexTask(transformClassesWithMultidexlistFor${variantName}) exist: $exist")

            if (multidexTask != null) {
                val replaceTask = createReplaceMainDexListTask(variant)
                multidexTask.finalizedBy(replaceTask)
            }
        }
    }
}

private fun Project.createReplaceMainDexListTask(variant: ApplicationVariant): Task {
    val variantName = variant.name.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
    }

    return task("replace${variantName}MainDexClassList").doLast {
        // 从主dex移除的列表
        // 存放剔除规则的路径
        val excludeClassList = project.projectDir.child {
            Contract.mappingFileSaveDirName / "main_dex_exclude_class.txt"
        }.takeIf { it.exists() }?.useLines {
            it.map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .toList()
        } ?: return@doLast

        // 主dex中类列表
        val mainDexFile = project.buildDir.child {
            "intermediates" / "legacy_multidex_main_dex_list" / variant.dirName / "transformClassesWithMultidexlistFor${variantName}"
        }.let {
            //再次判断兼容 linux/mac 环境获取
            it.child { "maindexlist.txt" }.takeIf { it.exists() }
                ?: it.child { "mainDexList.txt" }.takeIf { it.exists() }
        } ?: return@doLast

        val mainDexList = mainDexFile.useLines {
            it.map { it.trim() }.filter { it.isNotEmpty() }.toList()
        }
        val newMainDexList = mainDexList.mapNotNull { mainDexItem ->
            var isKeepMainDexItem = true
            for (excludeClassItem in excludeClassList) {
                if (mainDexItem.contains(excludeClassItem)) {
                    isKeepMainDexItem = false
                    break
                }
            }
            if (isKeepMainDexItem) mainDexItem else null
        }
        if (newMainDexList.size < mainDexList.size) {
            mainDexFile.delete()
            mainDexFile.createNewFile()
            mainDexFile.bufferedWriter().use { writer ->
                newMainDexList.forEach {
                    writer.append(it).appendLine()
                    writer.flush()
                }
            }
        }
    }
}
