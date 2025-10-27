package io.github.chenfei0928

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-10-22 16:57
 */
object Deps {
    //<editor-fold defaultstate="collapsed" desc="Kotlin">
    object kotlin {

        // https://github.com/Kotlin/kotlinx-datetime
        val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.7.1"

        // https://github.com/fengzhizi715/Lifecycle-Coroutines-Extension
        val coroutinesExt = "com.safframework.lifecycle:lifecycle-coroutine-ext:1.2.1"

        // Json序列化支持
        // https://github.com/Kotlin/kotlinx.serialization
        val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0"
        val protobuf = "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.9.0"

        // Ktor
        // https://ktor.io/docs/client-create-multiplatform-application.html#ktor-dependencies
        private const val ktor_version = "3.2.2"

        val ktorClientCore = "io.ktor:ktor-client-core:$ktor_version"
        val ktorClientOkhttp = "io.ktor:ktor-client-okhttp:$ktor_version"
        val ktorClientDarwin = "io.ktor:ktor-client-darwin:$ktor_version"

        // 服务端
        val ktorClientCio = "io.ktor:ktor-client-cio:$ktor_version"
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="网络工具">
    object network {
        // 下载框架
        // https://github.com/lingochamp/FileDownloader/blob/master/README-zh.md
        object filedownloader {
            val core = "com.liulishuo.filedownloader:library:1.7.8"
            val okhttp3 = "cn.dreamtobe.filedownloader:filedownloader-okhttp3-connection:1.0.0"
        }

        // 下载框架
        // https://github.com/AriaLyy/Aria
        object aria {
            val http = "me.laoyuyu.aria:http:3.8.16"
            val public = "me.laoyuyu.aria:public:3.8.16"

            val core = "me.laoyuyu.aria:core:3.8.16"
            val apt = "me.laoyuyu.aria:compiler:3.8.16"
            val ftp = "me.laoyuyu.aria:ftp:3.8.16"
            val sftp = "me.laoyuyu.aria:sftp:3.8.16"
            val m3u8 = "me.laoyuyu.aria:m3u8:3.8.16"
        }

        // https://github.com/lingochamp/okdownload/wiki
        object okDownload {
            private val latest_version = "1.0.7"

            // core
            val core = "com.liulishuo.okdownload:okdownload:${latest_version}"

            // provide sqlite to store breakpoints
            val sqlite = "com.liulishuo.okdownload:sqlite:${latest_version}"

            // provide okhttp to connect to backend
            // and then please import okhttp dependencies by yourself
            val okhttp = "com.liulishuo.okdownload:okhttp:${latest_version}"

            // provide interface to match filedownloader
            val fileDownloader = "com.liulishuo.okdownload:filedownloader:${latest_version}"

            // provide interface for kotlin extension
            val ktx = "com.liulishuo.okdownload:ktx:${latest_version}"
        }

        // 阿里OSS
        // https://help.aliyun.com/document_detail/32042.html
        // https://github.com/aliyun/aliyun-oss-android-sdk
        // https://mvnrepository.com/artifact/com.aliyun.dpa/oss-android-sdk
        val aliOss = "com.aliyun.dpa:oss-android-sdk:2.9.21"

        // https://github.com/qiniu/happy-dns-android
        val qiniuDns = "com.qiniu:happy-dns:2.0.1"

        // https://github.com/square/okhttp
        // https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
        object okhttp {
            val okhttp = "com.squareup.okhttp3:okhttp:5.2.1"
            val logging = "com.squareup.okhttp3:logging-interceptor:5.2.1"
        }

        // https://github.com/gildor/kotlin-coroutines-okhttp
        val okhttpExt = "ru.gildor.coroutines:kotlin-coroutines-okhttp:1.0"

        // https://github.com/square/retrofit
        // http://mvnrepository.com/artifact/com.squareup.retrofit2
        object retrofit {
            val core = "com.squareup.retrofit2:retrofit:3.0.0"
            val rxjava2 = "com.squareup.retrofit2:adapter-rxjava2:3.0.0"
            val gson = "com.squareup.retrofit2:converter-gson:3.0.0"
            val ktxs = "com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0"
            val protobuf = "com.squareup.retrofit2:converter-protobuf:3.0.0"
        }

        // https://github.com/franmontiel/PersistentCookieJar
        val cookieJar = "com.github.franmontiel:PersistentCookieJar:v1.0.1"

        // https://github.com/socketio/socket.io-client-java
        // 使用时要 exclude group: 'org.json', module: 'json'
        val ioSocket = "io.socket:socket.io-client:2.1.2"

        // https://github.com/square/picasso
        val picasso = "com.squareup.picasso:picasso:2.8"

        // https://hc.apache.org/httpcomponents-client-4.3.x/android-port.html
        val apacheClient = "org.apache.httpcomponents:httpclient-android:4.3.5.1"

        // https://developer.android.com/training/volley/index.html
        val volley = "com.android.volley:volley:1.2.1"

        // https://mvnrepository.com/artifact/org.apache.httpcomponents.core5/httpcore5-h2
        val apacheHttpCore5 = "org.apache.httpcomponents.core5:httpcore5-h2:5.2.2"
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Glide 图片加载库">
    object glide {
        // https://github.com/bumptech/glide
        private const val glideVer = "5.0.5"

        val core = "com.github.bumptech.glide:glide:$glideVer"
        val apt = "com.github.bumptech.glide:compiler:$glideVer"
        val ksp = "com.github.bumptech.glide:ksp:$glideVer"

        // https://mvnrepository.com/artifact/com.github.bumptech.glide/recyclerview-integration
        val recyclerview = "com.github.bumptech.glide:recyclerview-integration:$glideVer"

        // https://mvnrepository.com/artifact/com.github.bumptech.glide/okhttp3-integration
        val okhttp3 = "com.github.bumptech.glide:okhttp3-integration:$glideVer@aar"

        // https://github.com/zjupure/GlideWebpDecoder
        val webpDecoder = "com.zlc.glide:webpdecoder:2.7.$glideVer"

        // https://github.com/florent37/GlidePalette
        val glidePalette = "com.github.florent37:glidepalette:2.1.2"

        // https://github.com/wasabeef/glide-transformations
        val transformations = "jp.wasabeef:glide-transformations:4.3.0"
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="通用存放库">
    object lib {
        // 阻止三方SDK中常见的严重影响用户体验的『链式唤醒』行为
        // jcenter
        // https://github.com/oasisfeng/condom
        val condom = "com.oasisfeng.condom:library:2.5.0"

        // https://github.com/hotchemi/PermissionsDispatcher
        object permissionsDispatcher {
            // https://github.com/hotchemi/PermissionsDispatcher
            private const val permissionsDispatcherVer = "4.8.0"

            val core = "org.permissionsdispatcher:permissionsdispatcher:$permissionsDispatcherVer"
            val apt =
                "org.permissionsdispatcher:permissionsdispatcher-processor:$permissionsDispatcherVer"
        }

        // https://github.com/greenrobot/EventBus
        val eventbus = "org.greenrobot:eventbus:3.3.1"

        // https://github.com/leotyndale/EnFloatingView
        val floatView = "com.imuxuan:floatingview:1.6"

        // https://github.com/square/okio
        val okio = "com.squareup.okio:okio:3.10.2"

        // protobuf 序列化框架
        object protobuf {
            // https://github.com/grpc/grpc-java
            const val grpcVersion = "1.73.0"

            // https://github.com/grpc/grpc-kotlin
            const val grpcKotlinVersion = "1.4.1"

            // https://github.com/protocolbuffers/protobuf
            // https://github.com/protocolbuffers/protobuf/blob/main/java/lite.md
            // https://mvnrepository.com/artifact/com.google.protobuf/protobuf-javalite
            const val protobufVersion = "4.31.1"

            // https://github.com/protocolbuffers/protobuf/tree/master/java
            val java = "com.google.protobuf:protobuf-java:$protobufVersion"
            val util = "com.google.protobuf:protobuf-java-util:$protobufVersion"
            val kotlin = "com.google.protobuf:protobuf-kotlin:$protobufVersion"

            // https://github.com/protocolbuffers/protobuf/blob/master/java/lite.md
            val javaLite = "com.google.protobuf:protobuf-javalite:$protobufVersion"
            val kotlinLite = "com.google.protobuf:protobuf-kotlin-lite:$protobufVersion"
        }

        // https://github.com/Curzibn/Luban
        val luban = "top.zibin:Luban:1.1.8"

        // 可选，适用于Android的中国地区词典
        // https://github.com/promeG/TinyPinyin
        // https://github.com/biezhi/TinyPinyin
        val tinyPinyin = "io.github.biezhi:TinyPinyin:2.0.3.RELEASE"

        // https://github.com/niorgai/StatusBarCompat
        val statusBarCompat = "com.github.niorgai:StatusBarCompat:2.3.3"

        // https://github.com/jgilfelt/SystemBarTint
        val systemBarTint = "com.readystatesoftware.systembartint:systembartint:1.0.3"

        // https://github.com/chenfei0928/Util
        val util = "io.github.chenfei0928:util:1.3-SNAPSHOT"

        // https://github.com/getActivity/XXPermissions
        val xxPermission = "com.github.getActivity:XXPermissions:20.0"

        // https://github.com/johncarl81/parceler
        val parceler = "org.parceler:parceler-api:1.1.13"

        // Java标注
        // https://mvnrepository.com/artifact/com.google.code.findbugs/jsr305
        val jsr305 = "com.google.code.findbugs:jsr305:3.0.2"

        // https://repo1.maven.org/maven2/org/glassfish/javax.annotation/
//        val javax = "org.glassfish:javax.annotation:3.2-b06"
        val javax = "javax.annotation:javax.annotation-api:1.3.2"

        // JetBrains Java Annotations
        // 提供标示正则表达式等给ide提示代码字段用途的说明
        // https://mvnrepository.com/artifact/org.jetbrains/annotations
        val ideAnnotation = "org.jetbrains:annotations:26.0.2"

        // https://github.com/unicode-org/icu-demos/tree/main/icu4jweb
        // https://mvnrepository.com/artifact/com.ibm.icu/icu4j
        val icu4j = "com.ibm.icu:icu4j:71.1"

        // https://github.com/luben/zstd-jni
        val zstdCompression = "com.github.luben:zstd-jni:1.5.6-9@aar"

        // Google工具类
        object google {
            // https://github.com/google/guava
            val guavaAndroid = "com.google.guava:guava:33.5.0-android"

            // https://github.com/google/gson
            val gson = "com.google.code.gson:gson:2.13.2"

            // https://github.com/zxing/zxing
            // https://repo1.maven.org/maven2/com/google/zxing/core/
            val zxingCore = "com.google.zxing:core:3.5.3"

            // Java标注
            // https://mvnrepository.com/artifact/com.google.code.findbugs/jsr305
            val jsr305 = "com.google.code.findbugs:jsr305:3.0.2"
        }

        // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
        val apacheCommonLang3 = "org.apache.commons:commons-lang3:3.17.0"

        // https://github.com/Tencent/MMKV/blob/master/README_CN.md
//        val mmkv = "com.tencent:mmkv:1.3.12"
        val mmkv = "com.tencent:mmkv:2.2.4"

        // https://github.com/eclipse-collections/eclipse-collections
        val eclipseCollectionsApi = "org.eclipse.collections:eclipse-collections-api:11.1.0"
        val eclipseCollections = "org.eclipse.collections:eclipse-collections:11.1.0"

        // dagger2，依赖注入
        // https://github.com/google/dagger
        object dagger {
            // https://github.com/google/dagger
            private const val daggerVer = "2.34.1"

            val core = "com.google.dagger:dagger:$daggerVer"
            val support = "com.google.dagger:dagger-android-support:$daggerVer"
            val apt = "com.google.dagger:dagger-compiler:$daggerVer"
        }

        // https://github.com/alibaba/ARouter/blob/master/README_CN.md
        // https://github.com/jadepeakpoet/ARouter
        object aRouter {
            val api = "com.github.jadepeakpoet.ARouter:arouter-api:1.0.3"
            val compiler = "com.github.jadepeakpoet.ARouter:arouter-compiler:1.0.3"

            // 需在 buildSrc/build.gradle.kts 中同步依赖版本修改
            val plugin = "com.github.jadepeakpoet.ARouter:arouter-register:1.0.3"
        }

        object huawei {
            // https://developer.huawei.com/consumer/cn/doc/development/HMS-2-Library/hmssdk_huaweiid_sdkdownload
            const val hmsVersion = "2.6.3.306"

            // 需在 settings.gradle.kts 中同步 Maven 远端仓库地址修改
            val maven = "http://developer.huawei.com/repo/"

            // 华为基础库
            val base = "com.huawei.android.hms:base:$hmsVersion"

            // 华为登录
            // https://developer.huawei.com/consumer/cn/doc/development/HMS-2-Guides/hmssdk_huaweiid_devguide
            val id = "com.huawei.android.hms:hwid:$hmsVersion"

            // 华为支付
            // https://developer.huawei.com/consumer/cn/doc/development/HMS-2-Guides/hmssdk_huaweiiap_devguide_client_oversea
            val iap = "com.huawei.android.hms:iap:$hmsVersion"

            // 华为推送
            // https://developer.huawei.com/consumer/cn/doc/development/HMS-2-Guides/hmssdk_huaweipush_devguide_client_agent
            val push = "com.huawei.android.hms:push:$hmsVersion"
        }

        // 腾讯
        object tencent {
            // https://developers.weixin.qq.com/doc/oplatform/Downloads/Android_Resource.html
            val wechat = "com.tencent.mm.opensdk:wechat-sdk-android-without-mta:6.8.0"

            // https://bugly.qq.com/v2/
            val bugly = "com.tencent.bugly:crashreport:latest.release"

            // QQ官方SDK依赖库
            // https://wiki.connect.qq.com/%E4%BA%92%E8%81%94sdk%E6%94%AF%E6%8C%81maven
            val qq = "com.tencent.tauth:qqopensdk:3.52.0"

            // https://github.com/Tencent/mars#mars_cn
            val mars = "com.tencent.mars:mars-wrapper:1.2.5"
            val marsCore = "com.tencent.mars:mars-core:1.2.5"
            val marsXlog = "com.tencent.mars:mars-xlog:1.2.5"
        }

        // 穿山甲广告
        // https://www.pangle.cn/support/doc/5fcc8ef7253fee000ed14efa
        // https://www.pangle.cn/support/doc/5fcc8fcc253fee000ed14f33
        object pangle {
            // 需在 settings.gradle.kts 中同步 Maven 远端仓库地址修改
            val maven = "https://artifact.bytedance.com/repository/pangle"
            val adsSdk = "com.pangle.cn:ads-sdk-pro:4.3.0.8"
        }

        object baiduMap {
            val mapNavi = "com.baidu.lbsyun:BaiduMapSDK_Map-Navi:7.5.4"
            val location = "com.baidu.lbsyun:BaiduMapSDK_Location:9.3.7"
            val util = "com.baidu.lbsyun:BaiduMapSDK_Util:7.5.4"
            val search = "com.baidu.lbsyun:BaiduMapSDK_Search:7.5.4"
        }

        // https://github.com/apache/commons-compress
        const val apacheCompress = "org.apache.commons:commons-compress:1.27.1"

        // https://tukaani.org/xz/java.html
        // https://github.com/tukaani-project/xz-java
        const val xz = "org.tukaani:xz:1.10"
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="RxJava">
    object rx {
        // RxJava 响应式编程
        // https://github.com/ReactiveX/RxAndroid
        val android = "io.reactivex.rxjava2:rxandroid:2.1.1"
        val core = "io.reactivex.rxjava2:rxjava:2.2.19"

        // Rx生命周期，用于协调Activity生命周期变化取消订阅
        // https://github.com/trello/RxLifecycle
        val lifecycle = "com.trello.rxlifecycle3:rxlifecycle-android-lifecycle-kotlin:3.1.0"

        // https://github.com/JakeWharton/RxBinding
        val binding = "com.jakewharton.rxbinding3:rxbinding:3.1.0"

        // https://github.com/T-Spoon/Traceur
        val traceur = "com.tspoon.traceur:traceur:1.0.1"
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="控件">
    object widget {
        object recyclerView {
            // RecyclerView 的 ItemDecoration 分割线
            // https://github.com/yqritc/RecyclerView-FlexibleDivider
            val flexibleDivider = "com.yqritc:recyclerview-flexibledivider:1.4.0"

            // RecyclerView 动画库
            // https://github.com/wasabeef/recyclerview-animators
            val animators = "jp.wasabeef:recyclerview-animators:4.0.2"

            // https://github.com/rubensousa/RecyclerViewSnap
            val gravitySnap = "com.github.rubensousa:gravitysnaphelper:2.2.2"

            // https://github.com/TonicArtos/SuperSLiM
            val superSlim = "com.tonicartos:superslim:0.4.13"

            // https://github.com/drakeet/MultiType
            val multiType = "com.drakeet.multitype:multitype:4.3.0"

            // https://github.com/alibaba/vlayout/blob/master/README-ch.md
            // 版本：https://github.com/alibaba/vlayout/releases
            val vlayout = "com.alibaba.android:vlayout:1.2.36@aar"

            // https://github.com/bgogetap/StickyHeaders
            val stickyHeaders = "com.brandongogetap:stickyheaders:0.6.2"
        }

        // https://github.com/bingoogolapple/BGABadgeView-Android
        object badgeview {
            val core = "com.github.bingoogolapple.BGABadgeView-Android:api:1.2.0"
            val apt = "com.github.bingoogolapple.BGABadgeView-Android:compiler:1.2.0"
        }

        // https://github.com/Zhao-Yan-Yan/MultiStatePage
        val multiStatePage = "com.github.Zhao-Yan-Yan:MultiStatePage:2.0.6"

        // https://github.com/scwang90/SmartRefreshLayout/tree/master#%E7%AE%80%E5%8D%95%E7%94%A8%E4%BE%8B
        object smartRefreshLayout {
            val version = "2.0.3"

            // 核心必须依赖
            val layout_kernel = "com.scwang.smart:refresh-layout-kernel:$version"
            val drawable_paint = "com.scwang.smart:refresh-drawable-paint:$version"

            // 经典刷新头
            val header_classics = "com.scwang.smart:refresh-header-classics:$version"

            // 雷达刷新头
            val header_radar = "com.scwang.smart:refresh-header-radar:$version"

            // 虚拟刷新头
            val header_falsify = "com.scwang.smart:refresh-header-falsify:$version"

            // 谷歌刷新头
            val header_material = "com.scwang.smart:refresh-header-material:$version"

            // 二级刷新头
            val header_twoLevel = "com.scwang.smart:refresh-header-two-level:$version"

            // 球脉冲加载
            val footer_ball = "com.scwang.smart:refresh-footer-ball:$version"

            // 经典加载
            val footer_classics = "com.scwang.smart:refresh-footer-classics:$version"
        }

        // https://github.com/ikew0ng/SwipeBackLayout
        val swipeBackLayout = "me.imid.swipebacklayout.lib:library:1.1.0"

        val loadMoreWrapper = "com.github.nukc:LoadMoreWrapper:v1.9.1"

        // https://github.com/vanniktech/Emoji
        val emoji = "com.vanniktech:emoji-google:0.31.0"

        // https://github.com/H07000223/FlycoTabLayout/blob/master/README_CN.md
        val flycoTabLayout = "io.github.h07000223:flycoTabLayout:3.0.0"

        // https://github.com/bingoogolapple/BGABanner-Android
        val bgaBanner = "com.github.bingoogolapple:BGABanner-Android:3.0.1@aar"

        // https://github.com/google/flexbox-layout
        val flexbox = "com.google.android.flexbox:flexbox:3.0.0"

        // https://github.com/vinc3m1/RoundedImageView
        val roundedImageView = "com.makeramen:roundedimageview:2.3.0"

        // 搜索框
        // https://github.com/MiguelCatalan/MaterialSearchView
        val materialSearchView = "com.miguelcatalan:materialsearchview:1.4.0"

        // https://github.com/chrisbanes/PhotoView
        val photoView = "com.github.chrisbanes:PhotoView:2.3.0"

        // https://github.com/barteksc/AndroidPdfViewer
        val pdfview = "com.github.barteksc:android-pdf-viewer:3.2.0-beta.1"

        // https://github.com/daimajia/NumberProgressBar
        val numberProgressBar = "com.daimajia.numberprogressbar:library:1.4@aar"

        // https://github.com/airbnb/lottie-android
        val lottie = "com.airbnb.android:lottie:6.6.10"
        val lottieCompose = "com.airbnb.android:lottie-compose:6.6.10"

        // https://github.com/akexorcist/Android-RoundCornerProgressBar
        val roundCornerProgressBar = "com.akexorcist:round-corner-progress-bar:2.2.1"

        // https://github.com/PhilJay/MPAndroidChart
        val MPAndroidChart = "com.github.PhilJay:MPAndroidChart:v3.1.0"

        // https://github.com/patrykandpatrick/vico?tab=readme-ov-file
        // https://www.patrykandpatrick.com/vico/guide/stable/getting-started
        object vico {
            private val version = "2.2.1"

            // For Jetpack Compose.
            val compose = "com.patrykandpatrick.vico:compose:$version"

            // For Material 2 theming in Jetpack Compose.
            val composeM2 = "com.patrykandpatrick.vico:compose-m2:$version"

            // For Material 3 theming in Jetpack Compose.
            val composeM3 = "com.patrykandpatrick.vico:compose-m3:$version"

            // For Compose Multiplatform (Android, iOS, desktop).
            val multiplatform = "com.patrykandpatrick.vico:multiplatform:$version"

            // For Material 2 theming in Compose Multiplatform.
            val multiplatformM2 = "com.patrykandpatrick.vico:multiplatform-m2:$version"

            // For Material 3 theming in Compose Multiplatform.
            val multiplatformM3 = "com.patrykandpatrick.vico:multiplatform-m3:$version"

            // For the Android view system.
            val views = "com.patrykandpatrick.vico:views:$version"
        }

        // https://github.com/DreaminginCodeZH/MaterialProgressBar
        val materialProgressBar = "me.zhanghai.android.materialprogressbar:library:1.6.1"

        // https://github.com/Clans/FloatingActionButton
        val floatingActionButton = "com.github.clans:fab:1.6.4"

        // https://github.com/DmitryMalkovich/material-design-dimens
        val materialDimens = "com.dmitrymalkovich.android:material-design-dimens:1.4"

        // https://github.com/zzhoujay/RichText
        val richtext = "com.zzhoujay.richtext:richtext:3.0.8"

        // https://github.com/Carbs0126/ExpandableTextView
        val expandableTextView = "cn.carbs.android:ExpandableTextView:1.0.3"

        // https://github.com/armcha/ElasticView
        val elasticView = "com.github.armcha:ElasticView:0.2.0"

        // https://github.com/alphamu/PinEntryEditText
        val captchaInput = "com.alimuzaffar.lib:pinentryedittext:2.0.6"

        // https://github.com/lzyzsd/JsBridge
        // https://jitpack.io/#com.github.lzyzsd/jsbridge
        val jsBridge = "com.github.lzyzsd.jsbridge:jsbridge:2.0.0"

        // https://github.com/huburt-Hu/NewbieGuide
        val newbieGuide = "com.github.huburt-Hu:NewbieGuide:v2.4.4"

        // https://blog.csdn.net/lixinxiaos/article/details/100119695
        // https://github.com/ogaclejapan/SmartTabLayout
        val smartTab = "com.ogaclejapan.smarttablayout:library:2.0.0@aar"

        // https://github.com/hackware1993/MagicIndicator
        val magicIndicator = "com.github.hackware1993:MagicIndicator:1.7.0"

        // https://github.com/Bigkoo/Android-PickerView
        val pickerView = "com.contrarywind:Android-PickerView:4.1.9"

        // https://github.com/Dimezis/BlurView
        val blurView = "com.github.Dimezis:BlurView:version-2.0.4"

        // https://github.com/qinci/EdgeTranslucent
        val edgeTranslucent = "ren.qinc.edgetranslucent:lib:0.0.3"

        // https://github.com/getActivity/ToastUtils
        val toastUtil = "com.github.getActivity:ToastUtils:13.6"

        // 仿iOS的UIVisualEffectView
        // https://github.com/mmin18/RealtimeBlurView
        val realtimeBlurView = "com.github.mmin18:realtimeblurview:1.2.1"
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="播放器">
    object player {
        object ijk {
            // https://github.com/Bilibili/ijkplayer
            private const val ijkVer = "0.8.8"

            val core = "tv.danmaku.ijk.media:ijkplayer-java:$ijkVer"
            val exo = "tv.danmaku.ijk.media:ijkplayer-exo:$ijkVer"
            val armv5 = "tv.danmaku.ijk.media:ijkplayer-armv5:$ijkVer"
            val armv7a = "tv.danmaku.ijk.media:ijkplayer-armv7a:$ijkVer"
            val arm64 = "tv.danmaku.ijk.media:ijkplayer-arm64:$ijkVer"
            val x86 = "tv.danmaku.ijk.media:ijkplayer-x86:$ijkVer"
            val x86_64 = "tv.danmaku.ijk.media:ijkplayer-x86_64:$ijkVer"
        }

        object aliYun {
            // https://help.aliyun.com/document_detail/387486.html
            val externalPlayerAliyunArtp = "com.aliyun.sdk.android:AlivcArtp:5.1.5"

            //playerSDK
            // https://help.aliyun.com/document_detail/124711.html
            val externalPlayerFull = "com.aliyun.sdk.android:AliyunPlayer:5.4.0-full"
            val externalPlayerPart = "com.aliyun.sdk.android:AliyunPlayer:5.4.0-part"

            val externalPlayerAliyunArtc = "com.aliyun.sdk.android:AlivcArtc:5.4.0"

            val externalPlayerAliyunArtcNet = "com.aliyun.rts.android:RtsSDK:1.6.0"
        }

        object polyv {
            // 点播sdk
            // https://github.com/easefun/polyv-android-sdk-2.0-demo/releases
            // SDK核心包
            val player = "net.polyv.android:polyvPlayer:2.15.3"

            // SDK核心包
            val abi = "net.polyv.android:polyvPlayerABI:1.9.9"

            // SDK下载功能
            val download = "net.polyv.android:polyvDownload:2.15.3"

            // SDK上传功能
            val upload = "net.polyv.android:polyvUpload:2.3.3"

            // 评论gif
            val gif = "net.polyv.android:polyvGif:2.2.2"

            // 弹幕、截图功能中使用
            val sub = "net.polyv.android:polyvSub:2.15.2"

            // 直播sdk
            // https://github.com/easefun/polyv-android-live-sdk-2.0-demo/releases
            // SDK核心包
            val livePlayer = "com.easefun.polyv:polyvLivePlayer:2.8.0"

            // 云课堂
            // https://github.com/polyv/polyv-android-cloudClass-sdk-demo/releases
            val cloudClass = "net.polyv.android:polyvSDKCloudClass:0.16.4"
        }

        // https://github.com/Bilibili/DanmakuFlameMaster
        object danmaku {
            // https://github.com/Bilibili/DanmakuFlameMaster
            private const val danmakuVer = "0.9.24"

            val core = "com.github.ctiao:DanmakuFlameMaster:0.9.25"
            val armv5 = "com.github.ctiao:ndkbitmap-armv5:$danmakuVer"
            val armv7a = "com.github.ctiao:ndkbitmap-armv7a:$danmakuVer"
            val x86 = "com.github.ctiao:ndkbitmap-x86:$danmakuVer"
        }

        // https://github.com/linsea/UniversalVideoView
        val universal = "com.linsea:universalvideoview:1.1.0@aar"

        // https://github.com/tencent/vap/blob/master/Android/README.md
        val vap = "io.github.tencent:vap:2.0.25"
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="统计">
    object analytics {
        // 友盟
        // http://dev.umeng.com/sdk_integate/android_sdk/analytics_doc
        object umeng {
            // 需在 settings.gradle.kts 中同步 Maven 远端仓库地址修改
            val maven = "https://repo1.maven.org/maven2/"

            object analytics {
                // https://developer.umeng.com/docs/119267/detail/118578
                // https://repo1.maven.org/maven2/com/umeng/umsdk/common/maven-metadata.xml
                val common = "com.umeng.umsdk:common:9.4.7"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/asms/maven-metadata.xml
                val asms = "com.umeng.umsdk:asms:1.6.0"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/link/maven-metadata.xml
                val link = "com.umeng.umsdk:link:1.2.0"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/apm/maven-metadata.xml
                val apm = "com.umeng.umsdk:apm:1.5.2"

                // OAID
                // https://repo1.maven.org/maven2/com/umeng/umsdk/oaid_lenovo/maven-metadata.xml
                val lenovo = "com.umeng.umsdk:oaid_lenovo:1.0.0"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/oaid_mi/maven-metadata.xml
                val mi = "com.umeng.umsdk:oaid_mi:1.0.0"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/oaid_oppo/maven-metadata.xml
                val oppo = "com.umeng.umsdk:oaid_oppo:1.0.4"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/oaid_vivo/maven-metadata.xml
                val vivo = "com.umeng.umsdk:oaid_vivo:1.0.0.1"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/utdid/maven-metadata.xml
                val utdid = "com.umeng.umsdk:utdid:1.5.2.3"
            }

            object share {
                // https://developer.umeng.com/docs/128606/detail/193879
                // https://repo1.maven.org/maven2/com/umeng/umsdk/share-core/maven-metadata.xml
                val core = "com.umeng.umsdk:share-core:7.1.7"
                val board = "com.umeng.umsdk:share-board:7.1.7"
                val qq = "com.umeng.umsdk:share-qq:7.1.7"
                val wx = "com.umeng.umsdk:share-wx:7.1.7"
                val sina = "com.umeng.umsdk:share-sina:7.1.7"
                val alipay = "com.umeng.umsdk:share-alipay:7.1.7"
                val dingding = "com.umeng.umsdk:share-dingding:7.1.7"

                // https://github.com/sinaweibosdk/weibo_android_sdk
                val sinaCore = "io.github.sinaweibosdk:core:11.12.0@aar"
            }

            object uverify {
                // https://developer.umeng.com/docs/143070/detail/144780
                // https://repo1.maven.org/maven2/com/umeng/umsdk/uverify/maven-metadata.xml
                val core = "com.umeng.umsdk:uverify:2.5.5"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/uverify-main/maven-metadata.xml
                val main = "com.umeng.umsdk:uverify-main:2.1.3.2"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/uverify-logger/maven-metadata.xml
                val logger = "com.umeng.umsdk:uverify-logger:2.1.3.2"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/uverify-crashshield/maven-metadata.xml
                val crashShield = "com.umeng.umsdk:uverify-crashshield:2.1.3.2"
            }

            object network {
                // https://repo1.maven.org/maven2/com/umeng/umsdk/alicloud-httpdns/maven-metadata.xml
                val aliHttpdns = "com.umeng.umsdk:alicloud-httpdns:1.3.2.3.1"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/alicloud-utils/maven-metadata.xml
                val aliUtils = "com.umeng.umsdk:alicloud-utils:2.0.0"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/alicloud_beacon/maven-metadata.xml
                val aliBeacon = "com.umeng.umsdk:alicloud_beacon:1.0.5"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/agoo-accs/maven-metadata.xml
                val agooAccs = "com.umeng.umsdk:agoo-accs:3.4.2.7"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/agoo_networksdk/maven-metadata.xml
                val agooNetworksdk = "com.umeng.umsdk:agoo_networksdk:3.5.8"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/agoo_tlog/maven-metadata.xml
                val agooTlog = "com.umeng.umsdk:agoo_tlog:3.0.0.17"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/agoo_tnet4android/maven-metadata.xml
                val agooTnet4android = "com.umeng.umsdk:agoo_tnet4android:3.1.14.10"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/agoo_aranger/maven-metadata.xml
                val agooAranger = "com.umeng.umsdk:agoo_aranger:1.0.6"
            }

            object push {
                // 推送相关
                // https://developer.umeng.com/docs/66632/detail/98581
                // https://repo1.maven.org/maven2/com/umeng/umsdk/push/maven-metadata.xml
                val core = "com.umeng.umsdk:push:6.4.8"

                // 小米
                // http://admin.xmpush.xiaomi.com/zh_CN/mipush/downpage/android
                // https://developer.umeng.com/docs/66632/detail/98589#h2--push-sdk3
                // https://repo1.maven.org/maven2/com/umeng/umsdk/xiaomi-push/maven-metadata.xml
                val xiaomi = "com.umeng.umsdk:xiaomi-push:4.9.0"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/xiaomi-umengaccs/maven-metadata.xml
                val xiaomiAccs = "com.umeng.umsdk:xiaomi-umengaccs:1.2.7"

                // 华为
                // https://developer.umeng.com/docs/66632/detail/98589#h2--push-sdk10
                // https://repo1.maven.org/maven2/com/umeng/umsdk/huawei-umengaccs/maven-metadata.xml
                val huaweiAccs = "com.umeng.umsdk:huawei-umengaccs:1.3.6"

                // 魅族
                // http://open-wiki.flyme.cn/index.php
                // http://open-wiki.flyme.cn/doc-wiki/index#id?129
                // https://developer.umeng.com/docs/66632/detail/98589#h2--push-sdk18
                // https://repo1.maven.org/maven2/com/umeng/umsdk/meizu-push/maven-metadata.xml
//                        meizu     : 'com.umeng.umsdk:meizu-push:4.1.4',
                val meizu = "com.meizu.flyme.internet:push-internal:4.1.4"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/meizu-umengaccs/maven-metadata.xml
                val meizuAccs = "com.umeng.umsdk:meizu-umengaccs:1.1.5"

                // Oppo
                // https://open.oppomobile.com/wiki/doc#id=10741
                // https://developer.umeng.com/docs/66632/detail/98589#h2--oppo-push-sdk24
                // https://repo1.maven.org/maven2/com/umeng/umsdk/oppo-push/maven-metadata.xml
                val oppo = "com.umeng.umsdk:oppo-push:3.0.0"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/oppo-umengaccs/maven-metadata.xml
                val oppoAccs = "com.umeng.umsdk:oppo-umengaccs:1.0.8-fix"

                // Vivo
                // https://dev.vivo.com.cn/documentCenter/doc/364
                // https://developer.umeng.com/docs/66632/detail/98589#h2--vivo-push-sdk30
                // https://repo1.maven.org/maven2/com/umeng/umsdk/vivo-push/maven-metadata.xml
                val vivo = "com.umeng.umsdk:vivo-push:3.0.0.4"

                // https://repo1.maven.org/maven2/com/umeng/umsdk/vivo-umengaccs/maven-metadata.xml
                val vivoAccs = "com.umeng.umsdk:vivo-umengaccs:1.1.6"
            }
        }

        // 阿里百川SDK
        // https://developer.alibaba.com/docs/doc.htm?treeId=129&articleId=118697&docType=1
        // https://developer.alibaba.com/docs/doc.htm?treeId=129&articleId=105647&docType=1
        object ali {
            // ut 用于分佣打点追踪
            // http://repo.baichuan-android.taobao.com/content/groups/BaichuanRepositories/com/taobao/android/utdid4all/
            val utdId4all = "com.taobao.android:utdid4all:1.5.2"
            val utAnalytics = "com.taobao.android:ut-analytics:6.5.8.22"

            // 需在 settings.gradle.kts 中同步 Maven 远端仓库地址修改
            val maven =
                "http://repo.baichuan-android.taobao.com/content/groups/BaichuanRepositories/"
            // https://mvnrepository.com/artifact/com.aliyun.ams/alicloud-android-utdid
//                utdId: "com.aliyun.ams:alicloud-android-utdid:1.1.5.3"
//                maven: "http://maven.aliyun.com/nexus/content/repositories/releases/"
        }

        // 百度
        // https://mtj.baidu.com/web/sdk/index
        object baidu {
            // 需在 settings.gradle.kts 中同步 JCenter 远端仓库地址修改
            val jcenter = "https://repos.balad.ir/artifactory/jcenter"

            // https://repos.balad.ir/artifactory/jcenter/com/baidu/mobstat/mtj-sdk-circle/maven-metadata.xml
            val mtj = "com.baidu.mobstat:mtj-sdk-circle:4.0.6.0"

            // 需在 buildSrc/build.gradle.kts 中同步依赖版本修改
            // https://repos.balad.ir/artifactory/jcenter/com/baidu/mobstat/mtj-circle-plugin/maven-metadata.xml
            val plugin = "com.baidu.mobstat:mtj-circle-plugin:1.4.0"
        }

        // GrowingIO
        // https://docs.growingio.com/v3/developer-manual/sdkintegrated/android-sdk/auto-android-sdk
        object growingIo {
            val agent = "com.growingio.android:vds-android-agent:autotrack-2.9.4"

            // 需在 buildSrc/build.gradle.kts 中同步依赖版本修改
            val plugin = "com.growingio.android:vds-gradle-plugin:autotrack-2.9.4"
        }

        // 监控ANR事件
        // https://github.com/SalomonBrys/ANR-WatchDog
        val anrWatchDog = "com.github.anrwatchdog:anrwatchdog:1.4.0"
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Debug工具">
    object debug {
        // LeakCanary 内存泄漏检测工具
        // https://github.com/square/leakcanary
        // https://square.github.io/leakcanary/getting_started/
        val leakcanary = "com.squareup.leakcanary:leakcanary-android:2.14"
        val curtains = "com.squareup.curtains:curtains:1.2.3"

        // FaceBook的Stetho调试工具，可以在Chrome中直接调试APP，再次只作为Debug包时使用
        // http://www.devtf.cn/?p=135
        // http://blog.csdn.net/sbsujjbcy/article/details/45420475
        // https://github.com/facebook/stetho
        object stetho {
            val core = "com.facebook.stetho:stetho:1.6.0"
            val okhttp3 = "com.facebook.stetho:stetho-okhttp3:1.6.0"
            val js = "com.facebook.stetho:stetho-js-rhino:1.6.0"
        }

        // Facebook的Flipper调试工具
        object flipper {
            // https://fbflipper.com/docs/getting-started/android-native
            val core = "com.facebook.flipper:flipper:0.273.0"
            val soloader = "com.facebook.soloader:soloader:0.10.5"

            // https://fbflipper.com/docs/setup/network-plugin/
            val network = "com.facebook.flipper:flipper-network-plugin:0.273.0"

            // https://fbflipper.com/docs/setup/layout-plugin/
            val litho = "com.facebook.flipper:flipper-litho-plugin:0.273.0"
            val lithoAnnotations = "com.facebook.litho:litho-annotations:0.19.0"
        }

        // 网络监测工具
        object network {
            // https://github.com/qiniu/android-netdiag
            val netdiag = "com.qiniu:android-netdiag:0.1.1"
        }

        // https://bugly.qq.com/docs/user-guide/instruction-manual-android/?v=20230509233624
        val bugly = "com.tencent.bugly:crashreport:4.1.9.2"
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="框架">
    object framework {
        // Tinker热修复
        // https://github.com/Tencent/tinker
        const val tinker = "com.tencent.tinker:tinker-android-lib:1.9.15.2"

        // https://github.com/chachako/sweekt
        const val meowoolToolkit = "com.meowool.toolkit:sweekt:0.1.0-SNAPSHOT"

        // https://github.com/Qihoo360/RePlugin/blob/dev/README_CN.md
        private val rePluginVer = com.qihoo360.replugin.gradle.host.AppConstant.getVER().toString()
        val repluginHost = "com.qihoo360.replugin:replugin-host-lib:$rePluginVer"
        val repluginPlugin = "com.qihoo360.replugin:replugin-plugin-lib:$rePluginVer"
    }
    //</editor-fold>
}
