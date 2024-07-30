import io.github.chenfei0928.Deps
import io.github.chenfei0928.DepsAndroidx
import io.github.chenfei0928.android.applyApp
import io.github.chenfei0928.android.applyTest
import io.github.chenfei0928.compiler.applyKotlin
import io.github.chenfei0928.data.applyProtobuf

plugins {
    android
}

applyApp()
applyTest()
applyKotlin(parcelize = true)
applyProtobuf()

android {
    namespace = "io.github.chenfei0928.util"

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

    implementation(Deps.widget.recyclerView.multiType)

    implementation(Deps.lib.xxPermission)
    implementation("com.jaredrummler:android-processes:1.1.1")
//    implementation(Deps.lib.util) {
//        isChanging = "SNAPSHOT" in version.toString()
//    }
    implementation(projects.library)
}
