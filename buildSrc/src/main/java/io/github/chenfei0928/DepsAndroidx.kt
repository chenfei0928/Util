package io.github.chenfei0928

/**
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-19 15:44
 */
object DepsAndroidx {
    // https://developer.android.com/jetpack/androidx/versions
    private const val supportVer = "1.0.0"

    //<editor-fold defaultstate="collapsed" desc="核心库，包括Activity/Fragment">

    // https://developer.android.com/jetpack/androidx/releases/core
    // https://dl.google.com/dl/android/maven2/androidx/core/core/maven-metadata.xml
    const val core = "androidx.core:core:1.8.0"
    const val ktx = "androidx.core:core-ktx:1.8.0"

    // https://dl.google.com/dl/android/maven2/androidx/activity/activity/maven-metadata.xml
    const val activity = "androidx.activity:activity:1.5.0"
    const val activityKtx = "androidx.activity:activity-ktx:1.5.0"

    // https://dl.google.com/dl/android/maven2/androidx/activity/activity-compose/maven-metadata.xml
    const val activityCompose = "androidx.activity:activity-compose:1.5.0"

    // https://dl.google.com/dl/android/maven2/androidx/fragment/fragment/maven-metadata.xml
    const val fragment = "androidx.fragment:fragment:1.5.0"
    const val fragmentKtx = "androidx.fragment:fragment-ktx:1.5.0"

    // https://dl.google.com/dl/android/maven2/androidx/appcompat/appcompat/maven-metadata.xml
    const val appcompat = "androidx.appcompat:appcompat:1.4.2"
    const val appcompatRes = "androidx.appcompat:appcompat-resources:1.4.2"

    const val v4Legacy = "androidx.legacy:legacy-support-v4:1.0.0"

    // https://dl.google.com/dl/android/maven2/androidx/annotation/annotation/maven-metadata.xml
    const val annotation = "androidx.annotation:annotation:1.4.0"
    const val annotationExp = "androidx.annotation:annotation-experimental:1.4.0"

    // https://dl.google.com/dl/android/maven2/androidx/core/core-role/maven-metadata.xml
    const val role = "androidx.core:core-role:1.0.0"

    // https://dl.google.com/dl/android/maven2/androidx/core/core-performance/maven-metadata.xml
    const val performance = "androidx.core:core-performance:1.0.0-alpha02"

    // https://dl.google.com/dl/android/maven2/androidx/core/core-google-shortcuts/maven-metadata.xml
    const val shortcuts = "androidx.core:core-google-shortcuts:1.0.1"

    // https://dl.google.com/dl/android/maven2/androidx/core/core-remoteviews/maven-metadata.xml
    const val remoteViews = "androidx.core:core-remoteviews:1.0.0-beta01"

    // https://dl.google.com/dl/android/maven2/androidx/core/core-splashscreen/maven-metadata.xml
    const val splashScreen = "androidx.core:core-splashscreen:1.0.0-rc01"

    // 多dex文件支持
    object multidex {
        // https://dl.google.com/dl/android/maven2/androidx/multidex/multidex/maven-metadata.xml
        const val core = "androidx.multidex:multidex:2.0.1"

        // https://dl.google.com/dl/android/maven2/androidx/multidex/multidex-instrumentation/maven-metadata.xml
        const val instrumentation = "androidx.multidex:multidex-instrumentation:2.0.0"
    }
    //</editor-fold>

    // https://dl.google.com/dl/android/maven2/androidx/databinding/databinding-adapters/maven-metadata.xml
    object databinding {
        val adapters: String get() = "androidx.databinding:databinding-adapters:${Env.agpVersion}"
        val runtime: String get() = "androidx.databinding:databinding-runtime:${Env.agpVersion}"
        val common: String get() = "androidx.databinding:databinding-common:${Env.agpVersion}"
        val viewBinding: String get() = "androidx.databinding:viewbinding:${Env.agpVersion}"
    }

    //<editor-fold defaultstate="collapsed" desc="Utils">
    // https://developer.android.com/jetpack/androidx/releases/collection
    // https://dl.google.com/dl/android/maven2/androidx/collection/collection/maven-metadata.xml
    const val collection = "androidx.collection:collection:1.2.0"
    const val collectionKtx = "androidx.collection:collection-ktx:1.2.0"

    // https://dl.google.com/dl/android/maven2/androidx/asynclayoutinflater/asynclayoutinflater/maven-metadata.xml
    const val asyncLayoutInflater = "androidx.asynclayoutinflater:asynclayoutinflater:$supportVer"

    // https://developer.android.com/jetpack/androidx/releases/browser
    // https://dl.google.com/dl/android/maven2/androidx/browser/browser/maven-metadata.xml
    const val browser = "androidx.browser:browser:1.4.0"

    // https://developer.android.com/jetpack/androidx/releases/palette
    // https://dl.google.com/dl/android/maven2/androidx/palette/palette/maven-metadata.xml
    const val palette = "androidx.palette:palette:$supportVer"

    // https://developer.android.com/jetpack/androidx/releases/webkit
    // https://developer.android.com/reference/androidx/webkit/package-summary?hl=zh-cn
    // https://dl.google.com/dl/android/maven2/androidx/webkit/webkit/maven-metadata.xml
    const val webkit = "androidx.webkit:webkit:1.4.0"

    // https://developer.android.com/jetpack/androidx/releases/datastore
    // https://dl.google.com/dl/android/maven2/androidx/datastore/datastore-core/maven-metadata.xml
    object datastore {
        const val core = "androidx.datastore:datastore:1.0.0"
        const val sp = "androidx.datastore:datastore-preferences:1.0.0"
    }

    // https://developer.android.com/jetpack/androidx/releases/tracing
    // https://dl.google.com/dl/android/maven2/androidx/tracing/tracing/maven-metadata.xml
    const val trace = "androidx.tracing:tracing:1.0.0"
    const val traceKtx = "androidx.tracing:tracing-ktx:1.0.0"

    // https://developer.android.com/jetpack/androidx/releases/ads
    // https://dl.google.com/dl/android/maven2/androidx/ads/ads-identifier/maven-metadata.xml
    const val ads = "androidx.ads:ads-identifier:1.0.0-alpha04"

    // https://developer.android.com/jetpack/androidx/releases/startup
    // https://dl.google.com/dl/android/maven2/androidx/startup/startup-runtime/maven-metadata.xml
    const val startup = "androidx.startup:startup-runtime:1.1.1"
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Media">
    // https://dl.google.com/dl/android/maven2/androidx/media/media/maven-metadata.xml
    const val media = "androidx.media:media:1.4.3"

    // https://dl.google.com/dl/android/maven2/androidx/mediarouter/mediarouter/maven-metadata.xml
    const val mediarouter = "androidx.mediarouter:mediarouter:1.2.6"

    // https://dl.google.com/dl/android/maven2/androidx/media2/media2/maven-metadata.xml
    object media2 {
        const val core = "androidx.media2:media2:1.0.0-alpha04"
//                exo : "androidx.media2:media2-exoplayer:1.0.0-alpha03",
    }

    // https://developer.android.com/jetpack/androidx/releases/exifinterface
    // https://dl.google.com/dl/android/maven2/androidx/exifinterface/exifinterface/maven-metadata.xml
    const val exifInfo = "androidx.exifinterface:exifinterface:1.3.3"
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="UI">
    // https://developer.android.com/jetpack/androidx/releases/preference
    // https://dl.google.com/dl/android/maven2/androidx/preference/preference/maven-metadata.xml
    const val preference = "androidx.preference:preference:1.2.0"
    const val preferenceKtx = "androidx.preference:preference-ktx:1.2.0"

    // https://developer.android.com/jetpack/androidx/releases/vectordrawable
    object vector {
        // https://dl.google.com/dl/android/maven2/androidx/vectordrawable/vectordrawable/maven-metadata.xml
        const val drawable = "androidx.vectordrawable:vectordrawable:1.1.0"

        // https://dl.google.com/dl/android/maven2/androidx/vectordrawable/vectordrawable-animated/maven-metadata.xml
        const val animated = "androidx.vectordrawable:vectordrawable-animated:1.1.0"
    }

    object navigation {
        // https://developer.android.com/jetpack/androidx/releases/navigation
        val nav_version = "2.4.0"

        // Java language implementation
        val fragment = "androidx.navigation:navigation-fragment:$nav_version"
        val ui = "androidx.navigation:navigation-ui:$nav_version"

        // Kotlin
        val fragmentKtx = "androidx.navigation:navigation-fragment-ktx:$nav_version"
        val uiKtx = "androidx.navigation:navigation-ui-ktx:$nav_version"

        // Feature module Support
        val dynamicFragment =
            "androidx.navigation:navigation-dynamic-features-fragment:$nav_version"

        // Testing Navigation
        val testing = "androidx.navigation:navigation-testing:$nav_version"

        // Jetpack Compose Integration
        val compose = "androidx.navigation:navigation-compose:2.4.0-alpha10"
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Animation">
    // https://dl.google.com/dl/android/maven2/androidx/core/core-animation/maven-metadata.xml
    const val animation = "androidx.core:core-animation:1.0.0-alpha02"
    const val animationTest = "androidx.core:core-animation-testing:1.0.0-beta01"

    // https://dl.google.com/dl/android/maven2/androidx/transition/transition/maven-metadata.xml
    const val transition = "androidx.transition:transition:1.4.1"

    // https://dl.google.com/dl/android/maven2/androidx/interpolator/interpolator/maven-metadata.xml
    const val interpolator = "androidx.interpolator:interpolator:$supportVer"

    // https://dl.google.com/dl/android/maven2/androidx/dynamicanimation/dynamicanimation/maven-metadata.xml
    const val dynamicAnimation = "androidx.dynamicanimation:dynamicanimation:1.1.0-alpha03"
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Widget">
    // https://dl.google.com/dl/android/maven2/androidx/window/window/maven-metadata.xml
    const val window = "androidx.window:window:1.0.0"

    // https://dl.google.com/dl/android/maven2/androidx/viewpager/viewpager/maven-metadata.xml
    const val viewpager = "androidx.viewpager:viewpager:$supportVer"

    // https://developer.android.google.cn/jetpack/androidx/releases/viewpager2
    // https://dl.google.com/dl/android/maven2/androidx/viewpager2/viewpager2/maven-metadata.xml
    const val viewpager2 = "androidx.viewpager2:viewpager2:1.1.0-beta01"

    // https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout
    // https://dl.google.com/dl/android/maven2/androidx/swiperefreshlayout/swiperefreshlayout/maven-metadata.xml
    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01"

    // https://developer.android.com/jetpack/androidx/releases/cardview
    // https://dl.google.com/dl/android/maven2/androidx/cardview/cardview/maven-metadata.xml
    const val cardview = "androidx.cardview:cardview:$supportVer"

    // https://developer.android.com/jetpack/androidx/releases/coordinatorlayout
    // https://dl.google.com/dl/android/maven2/androidx/coordinatorlayout/coordinatorlayout/maven-metadata.xml
    const val coordinatorLayout = "androidx.coordinatorlayout:coordinatorlayout:1.2.0"

    // https://dl.google.com/dl/android/maven2/androidx/gridlayout/gridlayout/maven-metadata.xml
    const val gridlayout = "androidx.gridlayout:gridlayout:$supportVer"

    // https://dl.google.com/dl/android/maven2/com/google/android/material/material/maven-metadata.xml
    const val material = "com.google.android.material:material:1.6.1"

    // https://developer.android.com/jetpack/androidx/releases/recyclerview
    object recyclerview {
        // https://dl.google.com/dl/android/maven2/androidx/recyclerview/recyclerview/maven-metadata.xml
        const val core = "androidx.recyclerview:recyclerview:1.2.1"

        // https://dl.google.com/dl/android/maven2/androidx/recyclerview/recyclerview-selection/maven-metadata.xml
        const val selection = "androidx.recyclerview:recyclerview-selection:1.1.0"
    }

    // 约束布局
    // https://dl.google.com/dl/android/maven2/androidx/constraintlayout/constraintlayout/maven-metadata.xml
    // https://developer.android.com/reference/androidx/constraintlayout/widget/ConstraintLayout
    // https://developer.android.com/training/constraint-layout/index.html
    object constraintlayout {
        const val core = "androidx.constraintlayout:constraintlayout:2.1.4"
        const val solver = "androidx.constraintlayout:constraintlayout-solver:2.1.4"
    }
    //</editor-fold>

    // AI、ML

    //<editor-fold defaultstate="collapsed" desc="Tv/手表/汽车等特殊平台">

    // https://dl.google.com/dl/android/maven2/androidx/tvprovider/tvprovider/maven-metadata.xml
    const val tvProvider = "androidx.tvprovider:tvprovider:$supportVer"

    // https://dl.google.com/dl/android/maven2/androidx/leanback/leanback/maven-metadata.xml
    const val leanback = "androidx.leanback:leanback:1.1.0-alpha03"

    // https://dl.google.com/dl/android/maven2/androidx/recommendation/recommendation/maven-metadata.xml
    const val recommendation = "androidx.recommendation:recommendation:$supportVer"

    // https://dl.google.com/dl/android/maven2/androidx/leanback/leanback-preference/maven-metadata.xml
    const val leanbackPreference = "androidx.leanback:leanback-preference:1.1.0-alpha03"

    // https://dl.google.com/dl/android/maven2/androidx/wear/wear/maven-metadata.xml
    const val wear = "androidx.wear:wear:$supportVer"

    // https://dl.google.com/dl/android/maven2/androidx/car/car/maven-metadata.xml
    const val car = "androidx.car:car:1.0.0-alpha7"
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Loader and lifecycle">
    // Loader and lifecycle
    // https://dl.google.com/dl/android/maven2/androidx/arch/core/core-common/maven-metadata.xml
    object arch {
        // https://dl.google.com/dl/android/maven2/androidx/arch/core/core-common/maven-metadata.xml
        private const val archVer = "2.1.0"

        const val common = "androidx.arch.core:core-common:$archVer"
        const val testing = "androidx.arch.core:core-testing:$archVer"
        const val runtime = "androidx.arch.core:core-runtime:$archVer"
    }

    // https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-common/maven-metadata.xml
    object lifecycle {
        // https://dl.google.com/dl/android/maven2/androidx/lifecycle/lifecycle-common/maven-metadata.xml
        private const val lifecycleVer = "2.5.0"

        const val common = "androidx.lifecycle:lifecycle-common:$lifecycleVer"
        const val commonJ8 = "androidx.lifecycle:lifecycle-common-java8:$lifecycleVer"
        const val compiler = "androidx.lifecycle:lifecycle-compiler:$lifecycleVer"
        const val extensions = "androidx.lifecycle:lifecycle-extensions:2.2.0"
        const val livedata = "androidx.lifecycle:lifecycle-livedata:$lifecycleVer"
        const val livedataCore = "androidx.lifecycle:lifecycle-livedata-core:$lifecycleVer"
        const val livedataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVer"
        const val reactiveStreams = "androidx.lifecycle:lifecycle-reactivestreams:$lifecycleVer"
        const val runtime = "androidx.lifecycle:lifecycle-runtime:$lifecycleVer"
        const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVer"
        const val viewModel = "androidx.lifecycle:lifecycle-viewmodel:$lifecycleVer"
        const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVer"
        const val jetpackCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVer"
        const val service = "androidx.lifecycle:lifecycle-service:$lifecycleVer"
        const val process = "androidx.lifecycle:lifecycle-process:$lifecycleVer"
    }

    // https://developer.android.com/topic/libraries/architecture/paging
    // https://dl.google.com/dl/android/maven2/androidx/paging/paging-common/maven-metadata.xml
    object paging {
        // https://dl.google.com/dl/android/maven2/androidx/paging/paging-common/maven-metadata.xml
        private const val pagingVer = "3.1.0"

        const val common = "androidx.paging:paging-common:$pagingVer"
        const val runtime = "androidx.paging:paging-runtime:$pagingVer"
        const val rxjava2 = "androidx.paging:paging-rxjava2:$pagingVer"
    }

    // https://developer.android.com/topic/libraries/architecture/room
    // https://dl.google.com/dl/android/maven2/androidx/room/room-common/maven-metadata.xml
    object room {
        // https://dl.google.com/dl/android/maven2/androidx/room/room-common/maven-metadata.xml
        private const val roomVer = "2.4.2"

        const val common = "androidx.room:room-common:$roomVer"
        const val compiler = "androidx.room:room-compiler:$roomVer"
        const val guava = "androidx.room:room-guava:$roomVer"
        const val ktx = "androidx.room:room-ktx:$roomVer"
        const val migration = "androidx.room:room-migration:$roomVer"
        const val runtime = "androidx.room:room-runtime:$roomVer"
        const val rxjava2 = "androidx.room:room-rxjava2:$roomVer"
        const val testing = "androidx.room:room-testing:$roomVer"
    }

    // https://dl.google.com/dl/android/maven2/androidx/sqlite/sqlite/maven-metadata.xml
    object sqlite {
        // https://dl.google.com/dl/android/maven2/androidx/sqlite/sqlite/maven-metadata.xml
        private const val sqliteVer = "2.2.0"

        const val core = "androidx.sqlite:sqlite:$sqliteVer"
        const val framework = "androidx.sqlite:sqlite-framework:$sqliteVer"
    }

    // https://dl.google.com/dl/android/maven2/androidx/slice/slice-core/maven-metadata.xml
    object slice {
        // https://dl.google.com/dl/android/maven2/androidx/slice/slice-core/maven-metadata.xml
        private const val sliceVer = "1.1.0-alpha02"

        const val core = "androidx.slice:slice-core:$sliceVer"
        const val builders = "androidx.slice:slice-builders:$sliceVer"
        const val view = "androidx.slice:slice-view:$sliceVer"
    }

    // https://dl.google.com/dl/android/maven2/androidx/loader/loader/maven-metadata.xml
    const val loader = "androidx.loader:loader:1.1.0"

    // https://dl.google.com/dl/android/maven2/androidx/contentpager/contentpager/maven-metadata.xml
    const val contentPager = "androidx.contentpager:contentpager:$supportVer"

    // https://dl.google.com/dl/android/maven2/androidx/cursoradapter/cursoradapter/maven-metadata.xml
    const val cursorAdapter = "androidx.cursoradapter:cursoradapter:$supportVer"

    // https://dl.google.com/dl/android/maven2/androidx/documentfile/documentfile/maven-metadata.xml
    const val documentFile = "androidx.documentfile:documentfile:1.0.1"

    //</editor-fold>
}

// 开源通知
// https://developers.google.com/android/guides/opensource
object DepsGms {
    object oss {
        const val plugin = "com.google.android.gms:oss-licenses-plugin:0.10.4"
        const val core = "com.google.android.gms:play-services-oss-licenses:17.0.0"
    }
}

//<editor-fold defaultstate="collapsed" desc="Test">
object DepsTest {
    // https://dl.google.com/dl/android/maven2/androidx/test/espresso/espresso-core/maven-metadata.xml
    object espresso {
        // https://dl.google.com/dl/android/maven2/androidx/test/espresso/espresso-core/maven-metadata.xml
        private const val espressoVer = "3.4.0"

        object idling {
            const val concurrent = "androidx.test.espresso.idling:idling-concurrent:$espressoVer"
            const val net = "androidx.test.espresso.idling:idling-net:$espressoVer"
        }

        const val accessibility = "androidx.test.espresso:espresso-accessibility:$espressoVer"
        const val contrib = "androidx.test.espresso:espresso-contrib:$espressoVer"
        const val core = "androidx.test.espresso:espresso-core:$espressoVer"
        const val idlingRes = "androidx.test.espresso:espresso-idling-resource:$espressoVer"
        const val intents = "androidx.test.espresso:espresso-intents:$espressoVer"
        const val remote = "androidx.test.espresso:espresso-remote:$espressoVer"
        const val web = "androidx.test.espresso:espresso-web:$espressoVer"
    }

    // https://dl.google.com/dl/android/maven2/androidx/test/jank/janktesthelper/maven-metadata.xml
    const val jank = "androidx.test.jank:janktesthelper:1.0.1"

    // https://dl.google.com/dl/android/maven2/androidx/test/test-services/maven-metadata.xml
    const val testServices = "androidx.test:test-services:1.1.0"

    // https://dl.google.com/dl/android/maven2/androidx/test/uiautomator/uiautomator/maven-metadata.xml
    const val uiAutomator = "androidx.test.uiautomator:uiautomator:2.2.0"

    // https://dl.google.com/dl/android/maven2/androidx/test/monitor/maven-metadata.xml
    const val monitor = "androidx.test:monitor:1.4.0"

    // https://dl.google.com/dl/android/maven2/androidx/test/orchestrator/maven-metadata.xml
    const val orchestrator = "androidx.test:orchestrator:1.4.0"

    // https://dl.google.com/dl/android/maven2/androidx/test/rules/maven-metadata.xml
    const val rules = "androidx.test:rules:1.4.0"

    // https://dl.google.com/dl/android/maven2/androidx/test/runner/maven-metadata.xml
    const val runner = "androidx.test:runner:1.4.0"
}
//</editor-fold>
