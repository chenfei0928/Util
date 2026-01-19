import io.github.chenfei0928.Deps
import io.github.chenfei0928.DepsAndroidx
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
    versionName = "1.3",
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
    implementation(DepsAndroidx.core)
    implementation(DepsAndroidx.ktx)
    compileOnly(DepsAndroidx.webkit)
    implementation(DepsAndroidx.activity)
    implementation(DepsAndroidx.fragment)
    implementation(DepsAndroidx.startup)
    implementation(DepsAndroidx.appcompat)
    implementation(DepsAndroidx.documentFile)
    implementation(DepsAndroidx.annotation)
    implementation(DepsAndroidx.lifecycle.runtime)
    implementation(DepsAndroidx.lifecycle.runtimeKtx)
    compileOnly(DepsAndroidx.swipeRefreshLayout)
    compileOnly(DepsAndroidx.lifecycle.viewModel)
    compileOnly(DepsAndroidx.material)
    compileOnly(DepsAndroidx.localBroadcastManager)
    compileOnly(DepsAndroidx.recyclerview.core)
    compileOnly(DepsAndroidx.gridlayout)
    compileOnly(DepsAndroidx.preference)
    compileOnly(DepsAndroidx.datastore.core)
    compileOnly(DepsAndroidx.databinding.viewBinding)
    compileOnly(DepsAndroidx.databinding.runtime)
    compileOnly(DepsAndroidx.ads)
    compileOnly(DepsAndroidx.concurrentFuturesKtx)

    // https://github.com/bumptech/glide
    compileOnly(Deps.glide.core)
    compileOnly(Deps.glide.transformations)
    // https://github.com/google/gson
    compileOnly(Deps.lib.google.gson)
    testImplementation(Deps.lib.google.gson)
    compileOnly(Deps.lib.google.zxingCore)
    compileOnly(Deps.network.okhttp.okhttp)
    compileOnly(Deps.network.retrofit.core)
    compileOnly(Deps.lib.protobuf.java)
    compileOnly(Deps.lib.google.guavaAndroid)

    // ARouter
    compileOnly(Deps.lib.aRouter.api)

    compileOnly(Deps.widget.recyclerView.multiType)
    compileOnly(Deps.widget.flexbox)
    compileOnly(Deps.widget.jsBridge)
    compileOnly(Deps.kotlin.json)
    compileOnly(Deps.kotlin.protobuf)
    compileOnly(Deps.lib.mmkv)
}
