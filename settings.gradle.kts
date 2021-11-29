dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 原生镜像
        mavenCentral()
        google()
        // 腾讯云镜像
        // https://cloud.tencent.com/document/product/213/8623
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") {
            name = "qCloudMirrors"
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

include(
    ":app",
    "library",
)

//
//def initVcsVersionCode(isRelease) {
//    // 不是Release编译不更新版本号
//    if (!isRelease)
//        return 100000
//    def l = System.currentTimeMillis()
//    // 以commit数量从大到小排序
//    def ver
//    try {
//        ver = "git rev-list HEAD --count".execute().text.trim().toInteger()
//    } catch (e) {
//        System.err.println(e.toString())
//        e.printStackTrace()
//        ver = 0
//    }
//    println "VCS Version Code: ${ver}, time cost ${System.currentTimeMillis() - l} ms."
//    return ver
//}
//
//def initVcsCommitId(isRelease) {
//    // 不是Release编译不更新版本号
//    if (!isRelease)
//        return "-"
//    def l = System.currentTimeMillis()
//    def commitId = "git rev-parse HEAD".execute().text.trim()
//    println "VCS Commit Id: ${commitId}, time cost ${System.currentTimeMillis() - l} ms."
//    return commitId
//}
//
//def getIpAddress() {
//    try {
//        "\"${InetAddress.getLocalHost().getHostAddress()}\""
//    } catch (ignore) {
//        null
//    }
//}
//
//// 初始化编译环境
//// 初始化andResGuard白名单
//gradle.ext.andResGuard = []
//// 读取是否为正式编译、载入Vcs版本号
//def extMap = gradle.ext
//extMap.isRelease = false
//extMap.vcsVersion = 0
//new Thread({
//    extMap.vcsVersion = initVcsVersionCode(extMap.isRelease)
//    extMap.vcsCommitId = initVcsCommitId(extMap.isRelease)
//}).start()
//
