package io.github.chenfei0928.bean

import com.tencent.tinker.build.gradle.extension.TinkerArkHotExtension
import com.tencent.tinker.build.gradle.extension.TinkerBuildConfigExtension
import com.tencent.tinker.build.gradle.extension.TinkerDexExtension
import com.tencent.tinker.build.gradle.extension.TinkerLibExtension
import com.tencent.tinker.build.gradle.extension.TinkerPackageConfigExtension
import com.tencent.tinker.build.gradle.extension.TinkerPatchExtension
import com.tencent.tinker.build.gradle.extension.TinkerResourceExtension
import com.tencent.tinker.build.gradle.extension.TinkerSevenZipExtension
import groovy.lang.MetaClass
import org.gradle.api.Project

/**
 * @author chenf()
 * @date 2026-01-20 15:03
 */
class TinkerPatchExtensionAware(
    project: Project,
) : TinkerPatchExtension() {
    private var mataClass: MetaClass? = null
    override fun getMetaClass(): MetaClass? = mataClass

    override fun setMetaClass(p0: MetaClass?) {
        mataClass = p0
    }

    val buildConfig = TinkerBuildConfigExtension(project)
    val dex = TinkerDexExtension(project)
    val lib = TinkerLibExtension()
    val res = TinkerResourceExtension()
    val arkHot = TinkerArkHotExtension()
    val packageConfig = TinkerPackageConfigExtension(project)
    val sevenZip = TinkerSevenZipExtension(project)
}
