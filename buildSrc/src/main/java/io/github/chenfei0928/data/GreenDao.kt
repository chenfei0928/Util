package io.github.chenfei0928.data

import com.android.build.api.dsl.CommonExtension
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.implementation
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.greenrobot.greendao.gradle.Greendao3GradlePlugin
import org.greenrobot.greendao.gradle.GreendaoOptions
import org.joor.Reflect

/**
 * https://github.com/greenrobot/greenDAO
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-10-28 15:06
 */
fun Project.applyGreenDao(onlyDependencies: Boolean = true, schemaVersion: Int = 0) {
    if (!onlyDependencies) {
        apply<Greendao3GradlePlugin>()

        extensions.configure<GreendaoOptions>("greendao") {
            // 数据库版本号
            this.schemaVersion = schemaVersion
            // 生成数据库文件的目录
            targetGenDir("src/main/java")
            // 生成的数据库相关文件的包名
            daoPackage = "${buildSrcAndroid<CommonExtension>().namespace}.dao"
        }
    }

    // 从plugin中读取GreenDao版本号
    val greenDaoVersion: String = if (onlyDependencies) {
        Greendao3GradlePlugin()
    } else {
        plugins.getPlugin(Greendao3GradlePlugin::class.java)
    }.let { Reflect.on(it) }
        .call("getVersion")
        .get()

    dependencies {
        // https://github.com/yuweiguocn/GreenDaoUpgradeHelper
        implementation("io.github.yuweiguocn:GreenDaoUpgradeHelper:v2.2.1")
        // greendao
        // https://github.com/greenrobot/greenDAO
        implementation("org.greenrobot:greendao:$greenDaoVersion")
    }
}
