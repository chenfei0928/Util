/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-10-25 19:22
 */
package io.github.chenfei0928.util

import com.android.build.api.dsl.BuildType
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import io.github.chenfei0928.Contract
import io.github.chenfei0928.bean.TaskInfo
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import java.util.regex.Pattern

internal fun <Android : BaseExtension> Project.buildSrcAndroid(
    configure: Action<Android>
): Unit = extensions.configure("android", configure)

internal fun <Android : BaseExtension> Project.buildSrcAndroid(
): Android = extensions.getByName("android") as Android

/**
 * Android components
 * [Docs](https://developer.android.com/studio/build/extend-agp#variant-api-artifacts-tasks)
 *
 * @param configure
 */
internal fun <Android : AndroidComponentsExtension<*, *, *>> Project.buildSrcAndroidComponents(
    configure: Action<Android>
): Unit = extensions.configure("androidComponents", configure)

internal fun Project.checkApp(methodName: String) {
    extensions.getByName("android") as? BaseAppModuleExtension
        ?: throw IllegalStateException("$methodName 只能在 plugins android 下使用.")
}

//<editor-fold defaultstate="collapsed" desc="Dependencies">
internal fun DependencyHandlerScope.api(dependencyNotation: Any): Dependency? =
    add("api", dependencyNotation)

internal fun DependencyHandlerScope.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

internal fun DependencyHandlerScope.debugImplementation(dependencyNotation: Any): Dependency? =
    add("debugImplementation", dependencyNotation)

internal fun DependencyHandlerScope.compileOnly(dependencyNotation: Any): Dependency? =
    add("compileOnly", dependencyNotation)

internal fun DependencyHandlerScope.annotationProcessor(dependencyNotation: Any): Dependency? =
    add("annotationProcessor", dependencyNotation)

fun DependencyHandlerScope.kapt(dependencyNotation: Any): Dependency? =
    add("kapt", dependencyNotation)

fun DependencyHandlerScope.ksp(dependencyNotation: Any): Dependency? =
    add("ksp", dependencyNotation)

fun <T : ModuleDependency> T.excludeDep(dep: String): T =
    exclude(dep.split(":").let {
        mapOf("group" to it[0], "module" to it[1])
    }) as T

//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="BuildType">
internal fun <BuildTypeT : BuildType> NamedDomainObjectContainer<BuildTypeT>.release(
    action: BuildTypeT.() -> Unit
) = maybeCreate("release").apply(action)

fun <BuildTypeT : BuildType> NamedDomainObjectContainer<BuildTypeT>.prerelease(
    action: BuildTypeT.() -> Unit
) = maybeCreate("prerelease").apply(action)

fun <BuildTypeT : BuildType> NamedDomainObjectContainer<BuildTypeT>.qatest(
    action: BuildTypeT.() -> Unit
) = maybeCreate("qatest").apply(action)

internal fun <BuildTypeT : BuildType> NamedDomainObjectContainer<BuildTypeT>.debug(
    action: BuildTypeT.() -> Unit
) = maybeCreate("debug").apply(action)
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="遍历打包的assembleTask任务" >
/**
 * 对每个`assemble`开头的任务进行处理，在[Project.afterEvaluate]中执行，此时已生成task，
 * 这些任务是由Android Gradle Plugin产生的编译任务
 */
internal fun Project.forEachAssembleTasks(
    block: (assembleTask: Task, taskInfo: TaskInfo) -> Unit
) = forEachTasks(
    "assemble${Contract.DIMENSIONED_FLAVOR_AND_BUILD_TYPE}".toPattern(),
    Task::class.java,
    block
)

internal fun <TaskType : Task> Project.forEachTasks(
    taskNamePattern: Pattern,
    taskType: Class<TaskType>,
    block: (task: TaskType, taskInfo: TaskInfo) -> Unit,
) {
    val appExtension = buildSrcAndroid<AppExtension>()
    // 读取所有buildTypes
    val buildTypeNames by lazy {
        appExtension.buildTypes.map { it.name }
    }
    val flavorDimensions by lazy {
        appExtension.flavorDimensionList.toList()
    }
    val dimensionNameMap by lazy {
        val dimensionNameMap = flavorDimensions.associateWith { ArrayList<String>() }
        appExtension.productFlavors.forEach {
            dimensionNameMap[it.dimension]?.add(it.name)
        }
        dimensionNameMap
    }

    tasks.all {
        if (!taskType.isInstance(this)) {
            return@all
        }
        // 只对任务名匹配的任务进行处理
        val targetTaskName = name
        val matcher = taskNamePattern.matcher(targetTaskName)
        if (!matcher.find()) {
            return@all
        }
        val dimensionedFlavorBuildTypeName = matcher.group(1)
        // 只处理buildType为后缀的task，以过滤如美团Walle渠道包任务
        val buildType = buildTypeNames.find {
            dimensionedFlavorBuildTypeName.endsWith(it, true)
        } ?: return@all
        // 当前task的flavor名或空字符串（全flavor编译）
        val dimensionedFlavorName = dimensionedFlavorBuildTypeName
            .substring(0, dimensionedFlavorBuildTypeName.length - buildType.length)
            .replaceFirstCharToLowercase()
        val dimensionFlavorNames = run {
            var localDimensionedFlavorName = dimensionedFlavorName
            flavorDimensions.map { dimension ->
                val name = dimensionNameMap[dimension]?.find { name ->
                    localDimensionedFlavorName.startsWith(name, true)
                }?.also { name ->
                    localDimensionedFlavorName =
                        localDimensionedFlavorName.substring(
                            name.length, localDimensionedFlavorName.length
                        )
                }
                dimension to name
            }
        }

        // 对assemble编译任务进行处理
        block(
            taskType.cast(this),
            TaskInfo(
                dimensionFlavorNames,
                dimensionedFlavorName,
                buildType,
                dimensionedFlavorBuildTypeName
            )
        )
    }
}
//</editor-fold>
