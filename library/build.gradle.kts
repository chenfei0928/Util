import io.github.chenfei0928.Deps
import io.github.chenfei0928.DepsAndroidx
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
applyKotlin(parcelize = true, kapt = true)
withKotlinAndroidMavenPublication(
    versionName = "1.1-SNAPSHOT",
    signed = true,
    componentName = "debug"
)

android {
    buildFeatures {
        buildConfig = false
    }
}

dependencies {
    implementation(DepsAndroidx.core)
    implementation(DepsAndroidx.ktx)
    implementation(DepsAndroidx.webkit)
    implementation(DepsAndroidx.activity)
    implementation(DepsAndroidx.fragment)
    implementation(DepsAndroidx.startup)
    implementation(DepsAndroidx.lifecycle.runtime)
    implementation(DepsAndroidx.lifecycle.runtimeKtx)
    implementation(DepsAndroidx.lifecycle.viewModel)
    compileOnly(DepsAndroidx.material)
    compileOnly(DepsAndroidx.recyclerview.core)
    compileOnly(DepsAndroidx.gridlayout)
    compileOnly(DepsAndroidx.preference)
    compileOnly(DepsAndroidx.datastore.core)
    compileOnly(DepsAndroidx.databinding.viewBinding)
    compileOnly(DepsAndroidx.databinding.runtime)
    compileOnly(DepsAndroidx.ads)

    // https://github.com/bumptech/glide
    compileOnly(Deps.glide.core)
    compileOnly(Deps.glide.transformations)
    // https://github.com/google/gson
    compileOnly(Deps.lib.gson)
    compileOnly(Deps.lib.zxing.core)
    compileOnly(Deps.network.okhttp)
    compileOnly(Deps.network.retrofit.core)
    compileOnly(Deps.lib.protobuf.java)

    // ARouter
    compileOnly(Deps.lib.aRouter.api)

    compileOnly(Deps.widget.recyclerView.multiType)
    compileOnly(Deps.widget.flexbox)
    implementation(Deps.widget.jsBridge)
}
