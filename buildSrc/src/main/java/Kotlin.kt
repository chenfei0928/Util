import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-28 17:16
 */
fun Project.applyKotlin() {
    if (!hasKotlinPlugin()) {
        return
    }

    buildSrcAndroid<com.android.build.gradle.BaseExtension> {
        (this as org.gradle.api.plugins.ExtensionAware).extensions.configure<KotlinJvmOptions>(
            "kotlinOptions", Action<KotlinJvmOptions> {
                jvmTarget = JavaVersion.VERSION_1_8.toString()

                // Kotlin编译选项，可使用 kotlinc -X 查看
                // https://droidyue.com/blog/2019/07/21/configure-kotlin-compiler-options/
                if (Env.containsReleaseBuild) {
                    // Release编译时禁止参数非空检查
                    freeCompilerArgs = freeCompilerArgs + listOf(
                        "-Xno-param-assertions", "-Xno-receiver-assertions"
                    )
                }
            })
    }

    val kotlinPluginVersion = getKotlinPluginVersion()

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinPluginVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinPluginVersion")
        implementation(Deps.kotlin.coroutines)
        // Parcelable序列化支持
        implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:$kotlinPluginVersion")
    }
}

internal fun Project.hasKotlinPlugin(): Boolean =
    plugins.hasPlugin("kotlin-android")
