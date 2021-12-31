import me.omico.age.dsl.withKotlinAndroidMavenPublication

plugins {
    `android-library`
    `kotlin-android`
    `kotlin-parcelize`
    `maven-publish`
    signing
}

applyLib()
applyTest()
applyKotlin()
withKotlinAndroidMavenPublication(
    versionName = "1.0",
    signed = false,
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
    implementation(DepsAndroidx.material)
    implementation(DepsAndroidx.palette)
    implementation(DepsAndroidx.recyclerview.core)
    implementation(DepsAndroidx.gridlayout)
    implementation(DepsAndroidx.preference)
    implementation(DepsAndroidx.preferenceKtx)
    implementation(DepsAndroidx.multidex.core)
    implementation(DepsAndroidx.constraintlayout.core)
    implementation(DepsAndroidx.databinding.viewBinding)
    implementation(DepsAndroidx.databinding.adapters)
    implementation(DepsAndroidx.databinding.runtime)
    implementation(DepsAndroidx.ads)

    // https://github.com/bumptech/glide
    compileOnly(Deps.glide.core)
    // https://github.com/florent37/GlidePalette
    compileOnly(Deps.glide.glidePalette)
    // https://github.com/google/gson
    compileOnly(Deps.lib.gson)
    compileOnly(Deps.lib.zxing.core)
    compileOnly(Deps.network.okhttp)
    compileOnly(Deps.network.retrofit.core)
    compileOnly(Deps.lib.protobuf.java)

    // ARouter
    compileOnly(Deps.lib.aRouter.api)

    // 6.0 权限请求代理库
    compileOnly(Deps.widget.recyclerView.multiType)
    compileOnly(Deps.widget.flexbox)
    compileOnly(Deps.widget.jsBridge)
}
