import io.github.chenfei0928.android.applyApp
import io.github.chenfei0928.android.applyJetpackCompose
import io.github.chenfei0928.android.applyTest
import io.github.chenfei0928.compiler.applyKotlin
import io.github.chenfei0928.data.applyProtobuf

plugins {
    android
}

applyApp()
applyTest()
applyKotlin(parcelize = true, json = true, protobuf = true)
applyJetpackCompose()
applyProtobuf(includeGrpc = true)

android {
    namespace = "io.github.chenfei0928.demo"

    defaultConfig {
        applicationId = "io.github.chenfei0928.util"
        versionName = "1.0"
        minSdk = 23
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
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.media)

    implementation(libs.multi.type)

    implementation(libs.xxpermission)
    implementation(projects.library)
    implementation(libs.gson)
    implementation(libs.mmkv)
}