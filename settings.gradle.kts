dependencyResolutionManagement {
    enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        // 原生镜像
        mavenCentral()
        google()
        maven("https://jitpack.io") {
            name = "jetpack"
        }
        // 腾讯云镜像
        // https://cloud.tencent.com/document/product/213/8623
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") {
            name = "qCloudMirrors"
        }
        // 网易云镜像
        // https://mirrors.163.com/
        maven("https://mirrors.163.com/maven/repository/maven-central/") {
            name = "163Mirrors"
        }
        // 阿里云镜像
        // https://help.aliyun.com/document_detail/102512.html
        maven("https://maven.aliyun.com/repository/central") {
            name = "aliMirrors-central"
        }
        maven("https://maven.aliyun.com/repository/google") {
            name = "aliMirrors-google"
        }
        maven("http://maven.aliyun.com/nexus/content/repositories/releases") {
            name = "aliyun"
            isAllowInsecureProtocol = true
        }
    }
}

rootProject.name = "Util"
include(
    ":app",
    "library",
    ":script",
)
