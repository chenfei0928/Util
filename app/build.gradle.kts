import io.github.chenfei0928.Deps
import io.github.chenfei0928.DepsAndroidx
import io.github.chenfei0928.android.applyApp
import io.github.chenfei0928.android.applyTest
import io.github.chenfei0928.compiler.applyKotlin

plugins {
    android
}

applyApp()
applyTest()
applyKotlin()

android {
    defaultConfig {
        applicationId = "io.github.chenfei0928.util"
        versionName = "1.0"
    }
}

dependencies {
    implementation(DepsAndroidx.appcompat)
    implementation(DepsAndroidx.multidex.core)

    // RxJava 响应式编程
    implementation(Deps.rx.android)
    implementation(Deps.rx.core)
    // Rx生命周期，用于协调Activity生命周期变化取消订阅
    implementation(Deps.rx.lifecycle)

    // 网络
    implementation(Deps.network.okhttp)
    implementation(Deps.network.retrofit.core)
    implementation(Deps.network.retrofit.rxjava2)
    implementation(Deps.network.retrofit.gson)
    // Glide 图片加载库
    implementation(Deps.glide.core)
}
