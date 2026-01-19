package io.github.chenfei0928.compiler

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import io.github.chenfei0928.Contract
import io.github.chenfei0928.Deps
import io.github.chenfei0928.Env
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.debugImplementation
import io.github.chenfei0928.util.implementation
import io.github.chenfei0928.util.writeTmpProguardFile
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.internal.ParcelizeSubplugin
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin

/**
 * Kotlin语言、反射、协程配置
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-11-09 14:35
 */
fun Project.applyKotlin(
    parcelize: Boolean = false,
    kapt: Boolean = false,
    ksp: Boolean = false,
    json: Boolean = false,
    protobuf: Boolean = false,
) {
    if (parcelize) {
        apply<ParcelizeSubplugin>()
    }
    if (kapt) {
        apply<Kapt3GradleSubplugin>()
    }
    if (ksp) {
        apply<KspGradleSubplugin>()
    }
    if (json || protobuf) {
        apply<SerializationGradleSubplugin>()
    }

    buildSrcAndroid<com.android.build.gradle.BaseExtension>().apply {
        defaultConfig {
            if (Env.containsReleaseBuild) {
                // Release编译时移除kotlin断言
                proguardFile(
                    writeTmpProguardFile("kotlinParameterChecker.pro", proguardFileContent)
                )
            }
        }

        packagingOptions {
            resources.excludes += "DebugProbesKt.bin"
            // https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-debug#build-failures-due-to-duplicate-resource-files
            // for JNA and JNA-platform
            resources.excludes += "META-INF/AL2.0"
            resources.excludes += "META-INF/LGPL2.1"
            resources.excludes += "com/sun/jna/**"
            // for byte-buddy
            resources.excludes += "META-INF/licenses/ASM"
            resources.excludes += "win32-x86-64/**"
            resources.excludes += "win32-x86/**"
        }
    }

    buildSrcKotlin<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(Contract.JAVA_VERSION.toString()))

            // Kotlin编译选项，可使用 kotlinc -X 查看
            // https://droidyue.com/blog/2019/07/21/configure-kotlin-compiler-options/
            freeCompilerArgs.addAll(
                if (Env.containsReleaseBuild) {
                    // Release编译时禁止参数非空检查
                    listOf(
                        "-Xno-call-assertions",
                        "-Xno-param-assertions",
                        "-Xno-receiver-assertions",
                        "-Xannotation-target-all",
                        "-XXLanguage:+ExplicitBackingFields",
                    )
                } else {
                    // Debug编译时启用debug编译，以优化断点支持与断点时变量捕获
                    // https://kotlinlang.org/docs/whatsnew18.html#a-new-compiler-option-for-disabling-optimizations
                    listOf(
//                    "-Xdebug",
                        "-Xannotation-target-all",
                        "-XXLanguage:+ExplicitBackingFields",
                    )
                }
            )
        }
    }

    extensions.configure<KotlinAndroidProjectExtension>("kotlin") {
        sourceSets.all {
            languageSettings.enableLanguageFeature("ExplicitBackingFields")
            languageSettings.enableLanguageFeature("ExperimentalAtomicApi")
        }
    }

    dependencies {
        // stdlib 有jdk7和jdk8版本
        val jdkVersion = if (Contract.androidSdkToJdkVersion(Contract.minSdkVersion) >= 8) 8 else 7
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk$jdkVersion:$kotlinPluginVersion")
        implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinPluginVersion")
        // Parcelable序列化支持
        implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:$kotlinPluginVersion")
        // Json序列化支持
        // https://github.com/Kotlin/kotlinx.serialization
        if (json) {
            implementation(Deps.kotlin.json)
        }
        if (protobuf) {
            implementation(Deps.kotlin.protobuf)
        }
        // 协程库
        // https://github.com/Kotlin/kotlinx.coroutines
        val coroutinesVersion = "1.10.2"
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
        // Kotlin官方解决方案，需要在Kotlin协程库在入前调用 System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
        // https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/topics/debugging.md
        // https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-debug
        debugImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:$coroutinesVersion")
        // 此库可以将调用栈补充到被协程抛出的异常上，每网上抛出一次suspend/coroutineContext(?)切换均会追加一个调用栈
        // https://github.com/Anamorphosee/stacktrace-decoroutinator#android
//        debugImplementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-android:2.3.9")
    }
}

val kotlinPluginVersion = getKotlinPluginVersion(Env.logger)

//<editor-fold desc="Kotlin编译器插件扩展" defaultstate="collapsed">
internal val Project.hasKotlin: Boolean
    get() = plugins.hasPlugin(KotlinAndroidPluginWrapper::class.java)
internal val Project.hasKotlinKapt: Boolean
    get() = plugins.hasPlugin(Kapt3GradleSubplugin::class.java)
internal val Project.hasKotlinKsp: Boolean
    get() = plugins.hasPlugin(KspGradleSubplugin::class.java)

internal fun ExtensionAware.buildSrcKapt(block: KaptExtension.() -> Unit) =
    extensions.configure<KaptExtension>("kapt", block)

internal fun ExtensionAware.buildSrcKsp(block: KspExtension.() -> Unit) =
    extensions.configure<KspExtension>("ksp", block)

internal fun <Kotlin : KotlinProjectExtension> Project.buildSrcKotlin(
    configure: Kotlin.() -> Unit
): Unit = this.extensions.configure("kotlin", configure)
//</editor-fold>

// 移除kotlin断言
// https://gist.github.com/drakeet/e5235bffcf3d40a15831b92f6c4c769d
// https://www.guardsquare.com/blog/eliminating-data-leaks-caused-by-kotlin-assertions
private val proguardFileContent = """
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(...);
    public static void checkExpressionValueIsNotNull(...);
    public static void checkNotNullExpressionValue(...);
    public static void checkParameterIsNotNull(...);
    public static void checkNotNullParameter(...);
    public static void checkReturnedValueIsNotNull(...);
    public static void checkFieldIsNotNull(...);
    public static void throwUninitializedPropertyAccessException(...);
    public static void throwNpe(...);
    public static void throwJavaNpe(...);
    public static void throwAssert(...);
    public static void throwIllegalArgument(...);
    public static void throwIllegalState(...);
}
""".trimIndent()
