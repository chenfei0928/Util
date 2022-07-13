/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-10-25 19:22
 */
package io.github.chenfei0928.util

import io.github.chenfei0928.Contract
import io.github.chenfei0928.bean.AssembleTaskInfo
import org.gradle.api.*
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import java.util.*

internal fun <Android : com.android.build.gradle.BaseExtension> Project.buildSrcAndroid(
    configure: Action<Android>
): Unit = this.extensions.configure("android", configure)

internal fun <Android : com.android.build.gradle.BaseExtension> Project.buildSrcAndroid(
): Android = this.extensions.getByName("android") as Android

/**
 * Android components
 * [Docs](https://developer.android.com/studio/build/extend-agp#variant-api-artifacts-tasks)
 *
 * @param configure
 */
internal fun <Android : com.android.build.api.variant.AndroidComponentsExtension<*, *, *>> Project.buildSrcAndroidComponents(
    configure: Action<Android>
): Unit = (this as org.gradle.api.plugins.ExtensionAware).extensions.configure(
    "androidComponents", configure
)

internal fun Project.checkApp(methodName: String) {
    extensions.getByName("android") as? com.android.build.gradle.internal.dsl.BaseAppModuleExtension
        ?: throw IllegalStateException("$methodName 只能在 plugins android 下使用.")
}

//<editor-fold defaultstate="collapsed" desc="Dependencies">
internal fun DependencyHandlerScope.api(dependencyNotation: Any): Dependency? =
    add("api", dependencyNotation)

internal fun DependencyHandlerScope.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

internal fun DependencyHandlerScope.compileOnly(dependencyNotation: Any): Dependency? =
    add("compileOnly", dependencyNotation)

internal fun DependencyHandlerScope.annotationProcessor(dependencyNotation: Any): Dependency? =
    add("annotationProcessor", dependencyNotation)

fun DependencyHandlerScope.kapt(dependencyNotation: Any): Dependency? =
    add("kapt", dependencyNotation)

fun <T : ModuleDependency> T.excludeDep(dep: String): T =
    exclude(dep.split(":").let {
        mapOf("group" to it[0], "module" to it[1])
    }) as T

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="BuildType">
internal fun <BuildTypeT : com.android.build.api.dsl.BuildType> NamedDomainObjectContainer<BuildTypeT>.release(
    action: BuildTypeT.() -> Unit
) = maybeCreate("release").apply(action)

fun <BuildTypeT : com.android.build.api.dsl.BuildType> NamedDomainObjectContainer<BuildTypeT>.prerelease(
    action: BuildTypeT.() -> Unit
) = maybeCreate("prerelease").apply(action)

fun <BuildTypeT : com.android.build.api.dsl.BuildType> NamedDomainObjectContainer<BuildTypeT>.qatest(
    action: BuildTypeT.() -> Unit
) = maybeCreate("qatest").apply(action)

internal fun <BuildTypeT : com.android.build.api.dsl.BuildType> NamedDomainObjectContainer<BuildTypeT>.debug(
    action: BuildTypeT.() -> Unit
) = maybeCreate("debug").apply(action)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="遍历打包的assembleTask任务" >
/**
 * 对每个`assemble`开头的任务进行处理，这些任务是由Android Gradle Plugin产生的编译任务
 */
internal fun Project.assembleTasks(block: (assembleTask: Task, taskInfo: AssembleTaskInfo) -> Unit) {
    val buildTypeNames = mutableListOf<String>()

    buildSrcAndroid<com.android.build.gradle.AppExtension> {
        // 读取所有buildTypes
        buildTypes.forEach {
            buildTypeNames.add(it.name)
        }
    }

    tasks.all {
        // 只对 assemble[DimensionedFlavorName][BuildType] 的编译任务进行处理
        val targetTaskName = name
        if (!targetTaskName.startsWith(Contract.ASSEMBLE_TASK_PREFIX)) {
            return@all
        }
        // 只处理buildType为后缀的task，以过滤如美团Walle渠道包任务
        val buildType = buildTypeNames.find {
            targetTaskName.endsWith(it, true)
        } ?: return@all
        // 当前task的flavor名或空字符串（全flavor编译）
        val dimensionedFlavorName = targetTaskName
            .substring(
                Contract.ASSEMBLE_TASK_PREFIX.length,
                targetTaskName.length - buildType.length
            )
            .decapitalize(Locale.ROOT)

        block(this, AssembleTaskInfo(dimensionedFlavorName, buildType))
        if (dimensionedFlavorName.isEmpty()) {
            // 校验一下该全渠道包的task和依赖列表
            // 全渠道包的task只会包含所有同 buildType 的编译task
            this.dependsOn.flatMap { depend ->
                when (depend) {
                    is Collection<*> -> depend.map {
                        (it as? Named)?.name ?: it?.javaClass.toString()
                    }
                    is Task -> listOf(depend.name)
                    else -> listOf(depend?.javaClass.toString())
                }
            }.takeIf { dependsList ->
                dependsList.find {
                    !it.startsWith(Contract.ASSEMBLE_TASK_PREFIX)
                            || !it.endsWith(buildType, true)
                } != null
            }?.let {
                throw IllegalStateException("Task \"$targetTaskName\" depends tasks contains uncaught task, dependsOn $it.")
            }
            // 全渠道包的task不会拥有任何action，否则报错
            if (this.actions.isNotEmpty()) {
                throw IllegalStateException("Task \"$targetTaskName\" actions is not empty.")
            }
        }
    }
}
//</editor-fold>
