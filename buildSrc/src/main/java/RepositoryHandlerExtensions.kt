import org.gradle.api.Action
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-19 14:50
 */
fun RepositoryHandler.jcenter(
    url: Any, action: MavenArtifactRepository.() -> Unit
): MavenArtifactRepository = jcenter {
    setUrl(url)
    action()
}

fun org.gradle.api.Project.checkApp(methodName: String) {
    extensions.getByName("android") as? com.android.build.gradle.internal.dsl.BaseAppModuleExtension
        ?: throw IllegalStateException("$methodName 只能在 plugins android 下使用.")
}

internal fun <Android : com.android.build.gradle.BaseExtension> org.gradle.api.Project.buildSrcAndroid(
    configure: Action<Android>
): Unit = this.extensions.configure("android", configure)

internal fun DependencyHandlerScope.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

internal fun DependencyHandlerScope.annotationProcessor(dependencyNotation: Any): Dependency? =
    add("annotationProcessor", dependencyNotation)

internal fun DependencyHandlerScope.kapt(dependencyNotation: Any): Dependency? =
    add("kapt", dependencyNotation)

/**
 * The `kotlin-parcelize` plugin implemented by [org.jetbrains.kotlin.gradle.internal.ParcelizeSubplugin].
 */
val PluginDependenciesSpec.componentLibOrApp: PluginDependencySpec
    get() = this.id("android-library")
