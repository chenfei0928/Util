package io.github.chenfei0928.tinker

import com.google.gradle.osdetector.OsDetectorPlugin
import com.tencent.tinker.build.gradle.TinkerPatchPlugin
import com.tencent.tinker.build.gradle.extension.TinkerArkHotExtension
import com.tencent.tinker.build.gradle.extension.TinkerBuildConfigExtension
import com.tencent.tinker.build.gradle.extension.TinkerDexExtension
import com.tencent.tinker.build.gradle.extension.TinkerLibExtension
import com.tencent.tinker.build.gradle.extension.TinkerPackageConfigExtension
import com.tencent.tinker.build.gradle.extension.TinkerPatchExtension
import com.tencent.tinker.build.gradle.extension.TinkerResourceExtension
import com.tencent.tinker.build.gradle.extension.TinkerSevenZipExtension
import io.github.chenfei0928.Env
import io.github.chenfei0928.bean.TinkerPatchExtensionAware
import io.github.chenfei0928.util.buildOutputsDir
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType

/**
 * 应用Tinker官方Plugin，其尚未兼容7.1的AS-Gradle构建系统，暂不使用
 */
internal fun Project.applyTinkerPluginConfig(withPlugin: Boolean = true) {
    apply<OsDetectorPlugin>()

    if (withPlugin) {
        apply<TinkerPatchPlugin>()
    }

    createAndConfigTinkerPatchExtension(this, true)
}

internal fun ExtensionAware.createAndConfigTinkerPatchExtension(
    project: Project, useExtension: Boolean
): TinkerPatchExtension {
    val tinkerPatch = if (!useExtension) {
        TinkerPatchExtensionAware(project)
    } else {
        val tinkerPatch = extensions.create("tinkerPatch", TinkerPatchExtension::class.java)
        tinkerPatch as ExtensionAware

        tinkerPatch.extensions.create(
            "buildConfig",
            TinkerBuildConfigExtension::class.java,
            project
        )

        tinkerPatch.extensions.create("dex", TinkerDexExtension::class.java, project)
        tinkerPatch.extensions.create("lib", TinkerLibExtension::class.java)
        tinkerPatch.extensions.create("res", TinkerResourceExtension::class.java)
        tinkerPatch.extensions.create("arkHot", TinkerArkHotExtension::class.java)
        tinkerPatch.extensions.create(
            "packageConfig", TinkerPackageConfigExtension::class.java, project
        )
        tinkerPatch.extensions.create("sevenZip", TinkerSevenZipExtension::class.java, project)
        tinkerPatch
    }

    tinkerPatch.apply {
        /**
         * necessary，default 'null'
         * the old apk path, use to diff with the new apk to build
         * add apk from the build/bakApk
         */
        oldApk = null
        /**
         * optional，default 'false'
         * there are some cases we may get some warnings
         * if ignoreWarning is true, we would just assert the patch process
         * case 1: minSdkVersion is below 14, but you are using dexMode with raw.
         *         it must be crash when load.
         * case 2: newly added Android Component in AndroidManifest.xml,
         *         it must be crash when load.
         * case 3: loader classes in dex.loader{} are not keep in the main dex,
         *         it must be let tinker not work.
         * case 4: loader classes in dex.loader{} changes,
         *         loader classes is ues to load patch dex. it is useless to change them.
         *         it won't crash, but these changes can't effect. you may ignore it
         * case 5: resources.arsc has changed, but we don't use applyResourceMapping to build
         */
        ignoreWarning = true

        /**
         * optional，default 'true'
         * whether sign the patch file
         * if not, you must do yourself. otherwise it can't check success during the patch loading
         * we will use the sign config with your build type
         */
        useSign = true

        /**
         * optional，default 'true'
         * whether use tinker to build
         */
        tinkerEnable = true

        outputFolder = project.buildOutputsDir.absolutePath

        /**
         * Warning, applyMapping will affect the normal android build!
         */
        buildConfig {
            /**
             * optional，default 'null'
             * if we use tinkerPatch to build the patch apk, you'd better to apply the old
             * apk mapping file if minifyEnabled is enable!
             * Warning:
             * you must be careful that it will affect the normal assemble build!
             */
            applyMapping = null
            /**
             * optional，default 'null'
             * It is nice to keep the resource id from R.txt file to reduce java changes
             */
            applyResourceMapping = null

            /**
             * necessary，default 'null'
             * because we don't want to check the base apk with md5 in the runtime(it is slow)
             * tinkerId is use to identify the unique base apk when the patch is tried to apply.
             * we can use git rev, svn rev or simply versionCode.
             * we will gen the tinkerId in your manifest automatic
             */
            tinkerId = null

            /**
             * if keepDexApply is true, class in which dex refer to the old apk.
             * open this can reduce the dex diff file size.
             */
            keepDexApply = false

            /**
             * optional, default 'false'
             * Whether tinker should treat the base apk as the one being protected by app
             * protection tools.
             * If this attribute is true, the generated patch package will contain a
             * dex including all changed classes instead of any dexdiff patch-info files.
             */
            isProtectedApp = false

            /**
             * optional, default 'false'
             * Whether tinker should support component hotplug (add new component dynamically).
             * If this attribute is true, the component added in new apk will be available after
             * patch is successfully loaded. Otherwise an error would be announced when generating patch
             * on compile-time.
             *
             * <b>Notice that currently this feature is incubating and only support NON-EXPORTED Activity</b>
             */
            supportHotplugComponent = false
        }

        dex {
            /**
             * optional，default 'jar'
             * only can be 'raw' or 'jar'. for raw, we would keep its original format
             * for jar, we would repack dexes with zip format.
             * if you want to support below 14, you must use jar
             * or you want to save rom or check quicker, you can use raw mode also
             */
            dexMode = "jar"

            /**
             * necessary，default '[]'
             * what dexes in apk are expected to deal with tinkerPatch
             * it support * or ? pattern.
             */
            pattern = listOf("classes*.dex", "assets/secondary-dex-?.jar")
            /**
             * necessary，default '[]'
             * Warning, it is very very important, loader classes can't change with patch.
             * thus, they will be removed from patch dexes.
             * you must put the following class into main dex.
             * Simply, you should add your own application {@code tinker.sample.android.SampleApplication}
             * own tinkerLoader, and the classes you use in them
             *
             */
            loader = listOf(
                //use sample, let BaseBuildInfo unchangeable with tinker
                "com.tencent.tinker.loader.*",
                "com.xi.quickgame.base.app.SampleApplication",
                // aosproxy中定义的os内置类，用于无反射访问os内部接口
                // 由于tinker自身也会通过反射访问，它又在apk代码中，所以差异包生成工具会认为在loader内访问apk自身代码而报警
                "dalvik.system.VMRuntime"
            )
        }

        lib {
            /**
             * optional，default '[]'
             * what library in apk are expected to deal with tinkerPatch
             * it support * or ? pattern.
             * for library in assets, we would just recover them in the patch directory
             * you can get them in TinkerLoadResult with Tinker
             */
            pattern = listOf("lib/*/*.so")
        }

        arkHot {
            path = "arkHot"
            name = "patch.apk"
        }

        res {
            /**
             * optional，default '[]'
             * what resource in apk are expected to deal with tinkerPatch
             * it support * or ? pattern.
             * you must include all your resources in apk here,
             * otherwise, they won't repack in the new apk resources.
             */
            pattern = listOf("res/*", "assets/*", "resources.arsc", "AndroidManifest.xml")

            /**
             * optional，default '[]'
             * the resource file exclude patterns, ignore add, delete or modify resource change
             * it support * or ? pattern.
             * Warning, we can only use for files no relative with resources.arsc
             */
            ignoreChange = listOf("assets/sample_meta.txt")

            /**
             * default 100kb
             * for modify resource, if it is larger than 'largeModSize'
             * we would like to use bsdiff algorithm to reduce patch file size
             */
            largeModSize = 100
        }

        packageConfig {
            /**
             * optional，default 'TINKER_ID, TINKER_ID_VALUE' 'NEW_TINKER_ID, NEW_TINKER_ID_VALUE'
             * package meta file gen. path is assets/package_meta.txt in patch file
             * you can use securityCheck.getPackageProperties() in your ownPackageCheck method
             * or TinkerLoadResult.getPackageConfigByName
             * we will get the TINKER_ID from the old apk manifest for you automatic,
             * other config files (such as patchMessage below)is not necessary
             */
            configField("patchMessage", "tinker is sample to use")
            /**
             * just a sample case, you can use such as sdkVersion, brand, channel...
             * you can parse it in the SamplePatchListener.
             * Then you can use patch conditional!
             */
            configField("platform", "all")
            /**
             * patch version via packageConfig
             */
            configField("patchVersion", "1.0")
        }
        //or you can add config filed outside, or get meta value from old apk
        //project.tinkerPatch.packageConfig.configField("test1", project.tinkerPatch.packageConfig.getMetaDataFromOldApk("Test"))
        //project.tinkerPatch.packageConfig.configField("test2", "sample")

        /**
         * if you don't use zipArtifact or path, we just use 7za to try
         */
        sevenZip {
            /**
             * optional，default '7za'
             * the 7zip artifact path, it will use the right 7za with your platform
             */
//            zipArtifact = "com.tencent.mm:SevenZip:1.1.10"
            /**
             * optional，default '7za'
             * you can specify the 7za path yourself, it will overwrite the zipArtifact value
             */
            path = if (Env.isWindows) "C:\\Program Files\\7-Zip\\7z.exe" else "/usr/local/bin/7za"
        }
    }
    return tinkerPatch
}

private fun ExtensionAware.tinkerPatch(action: Action<TinkerPatchExtension>) =
    extensions.configure("tinkerPatch", action)

internal val TinkerPatchExtension.buildConfig: TinkerBuildConfigExtension
    get() = if (this is TinkerPatchExtensionAware) this.buildConfig
    else (this as ExtensionAware).extensions.getByType()

internal fun TinkerPatchExtension.buildConfig(action: Action<TinkerBuildConfigExtension>) =
    if (this is TinkerPatchExtensionAware) action.execute(this.buildConfig)
    else (this as ExtensionAware).extensions.configure("buildConfig", action)

internal val TinkerPatchExtension.dex: TinkerDexExtension
    get() = if (this is TinkerPatchExtensionAware) this.dex
    else (this as ExtensionAware).extensions.getByType()

internal fun TinkerPatchExtension.dex(action: Action<TinkerDexExtension>) =
    if (this is TinkerPatchExtensionAware) action.execute(this.dex)
    else (this as ExtensionAware).extensions.configure("dex", action)

internal val TinkerPatchExtension.lib: TinkerLibExtension
    get() = if (this is TinkerPatchExtensionAware) this.lib
    else (this as ExtensionAware).extensions.getByType()

internal fun TinkerPatchExtension.lib(action: Action<TinkerLibExtension>) =
    if (this is TinkerPatchExtensionAware) action.execute(this.lib)
    else (this as ExtensionAware).extensions.configure("lib", action)

internal val TinkerPatchExtension.res: TinkerResourceExtension
    get() = if (this is TinkerPatchExtensionAware) this.res
    else (this as ExtensionAware).extensions.getByType()

internal fun TinkerPatchExtension.res(action: Action<TinkerResourceExtension>) =
    if (this is TinkerPatchExtensionAware) action.execute(this.res)
    else (this as ExtensionAware).extensions.configure("res", action)

internal val TinkerPatchExtension.arkHot: TinkerArkHotExtension
    get() = if (this is TinkerPatchExtensionAware) this.arkHot
    else (this as ExtensionAware).extensions.getByType()

internal fun TinkerPatchExtension.arkHot(action: Action<TinkerArkHotExtension>) =
    if (this is TinkerPatchExtensionAware) action.execute(this.arkHot)
    else (this as ExtensionAware).extensions.configure("arkHot", action)

internal val TinkerPatchExtension.packageConfig: TinkerPackageConfigExtension
    get() = if (this is TinkerPatchExtensionAware) this.packageConfig
    else (this as ExtensionAware).extensions.getByType()

internal fun TinkerPatchExtension.packageConfig(action: Action<TinkerPackageConfigExtension>) =
    if (this is TinkerPatchExtensionAware) action.execute(this.packageConfig)
    else (this as ExtensionAware).extensions.configure("packageConfig", action)

internal val TinkerPatchExtension.sevenZip: TinkerSevenZipExtension
    get() = if (this is TinkerPatchExtensionAware) this.sevenZip
    else (this as ExtensionAware).extensions.getByType()

internal fun TinkerPatchExtension.sevenZip(action: Action<TinkerSevenZipExtension>) =
    if (this is TinkerPatchExtensionAware) action.execute(this.sevenZip)
    else (this as ExtensionAware).extensions.configure("sevenZip", action)
