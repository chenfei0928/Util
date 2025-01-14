import io.github.chenfei0928.Deps
import io.github.chenfei0928.DepsAndroidx
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
applyProtobuf()

android {
    namespace = "io.github.chenfei0928.demo"

    defaultConfig {
        applicationId = "io.github.chenfei0928.util"
        versionName = "1.0"
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
    implementation(DepsAndroidx.appcompat)
    implementation(DepsAndroidx.constraintlayout.layout)
    implementation(DepsAndroidx.multidex.core)
    implementation(DepsAndroidx.preference)
    implementation(DepsAndroidx.lifecycle.service)
    implementation(DepsAndroidx.datastore.core)
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.21")

    implementation(Deps.widget.recyclerView.multiType)

    implementation(Deps.lib.xxPermission)
//    implementation(Deps.lib.util) {
//        isChanging = "SNAPSHOT" in version.toString()
//    }
    implementation(projects.library)
    implementation(Deps.lib.google.gson)
}
