import me.omico.age.dsl.withKotlinAndroidMavenPublication

plugins {
    `android-library`
    `kotlin-android`
    `kotlin-parcelize`
    `maven-publish`
    signing
}

applyLibrary(appendBuildConfig = false)
applyTest()
applyKotlin()
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
    implementation(DepsAndroidx.lifecycle.runtime)
    implementation(DepsAndroidx.lifecycle.viewModel)
    implementation(DepsAndroidx.material)
    implementation(DepsAndroidx.recyclerview.core)
    implementation(DepsAndroidx.gridlayout)
    compileOnly(DepsAndroidx.preference)
    implementation(DepsAndroidx.constraintlayout.core)
    implementation(DepsAndroidx.databinding.viewBinding)
    implementation(DepsAndroidx.databinding.runtime)
    implementation(DepsAndroidx.ads)

    // https://github.com/bumptech/glide
    implementation(Deps.glide.core)
    // https://github.com/google/gson
    implementation(Deps.lib.gson)
    compileOnly(Deps.lib.zxing.core)
    compileOnly(Deps.network.okhttp)
    compileOnly(Deps.network.retrofit.core)
    compileOnly(Deps.lib.protobuf.java)

    // ARouter
    implementation(Deps.lib.aRouter.api)

    implementation(Deps.widget.recyclerView.multiType)
    implementation(Deps.widget.flexbox)
    implementation(Deps.widget.jsBridge)
}
