import io.github.chenfei0928.Env
import io.github.chenfei0928.android.applyLibrary
import io.github.chenfei0928.android.applyTest
import io.github.chenfei0928.compiler.applyKotlin
import me.omico.age.dsl.withKotlinAndroidMavenPublication

plugins {
    `android-library`
    `maven-publish`
    signing
}

applyLibrary(appendBuildConfig = false)
applyTest()
applyKotlin(parcelize = true)
withKotlinAndroidMavenPublication(
    versionName = "1.4-SNAPSHOT",
    signed = true,
)

android {
    namespace = "io.github.chenfei0928.util"
    resourcePrefix = "cf0928util_"

    defaultConfig {
        minSdk = 21

        buildConfigField("String", "vcsCommitId", "\"${Env.vcsCommitId}\"")
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    packaging {
        resources {
            excludes += arrayOf(
                "META-INF/LICENSE-notice.md",
                "META-INF/LICENSE.md",
                "MANIFEST.MF",
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.ktx)
    compileOnly(libs.androidx.webkit)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    compileOnly(libs.androidx.swiperefreshlayout)
    compileOnly(libs.androidx.lifecycle.viewmodel)
    compileOnly(libs.material)
    compileOnly(libs.androidx.localbroadcastmanager)
    compileOnly(libs.androidx.recyclerview)
    compileOnly(libs.androidx.gridlayout)
    compileOnly(libs.androidx.preference)
    compileOnly(libs.androidx.datastore.core)
    compileOnly(libs.androidx.viewbinding)
    compileOnly(libs.androidx.databinding.runtime)
    compileOnly(libs.androidx.concurrent.futures.ktx)

    compileOnly(libs.glide)
    compileOnly(libs.glide.transformations)
    compileOnly(libs.gson)
    testImplementation(libs.gson)
    compileOnly(libs.zxing.core)
    compileOnly(libs.okhttp)
    compileOnly(libs.retrofit)
    compileOnly(libs.protobuf.java)
    compileOnly(libs.guava.android)

    compileOnly(libs.arouter.api)

    compileOnly(libs.multi.type)
    compileOnly(libs.flexbox)
    compileOnly(libs.jsbridge)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.protobuf)
    compileOnly(libs.mmkv)
}