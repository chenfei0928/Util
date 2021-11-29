import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * 使用打包脚本CLI进行处理，此处不依赖plugin
 * 美团渠道号工具打包流程与微信资源文件名混淆插件无法同时使用（都是通过添加task名实现处理，无法同时使用两者）
 * 美团官方维护的版本不支持V3签名，使用的版本为：https://github.com/Meituan-Dianping/walle/issues/264
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-07-31 17:30
 */
fun Project.applyWalle() {
    checkApp("applyWalle")

    // 使用打包脚本CLI进行处理，此处不依赖plugin
    // 美团渠道号工具打包流程与微信资源文件名混淆插件无法同时使用（都是通过添加task名实现处理，无法同时使用两者）
    // 美团官方维护的版本不支持V3签名，使用的版本为：https://github.com/Meituan-Dianping/walle/issues/264
    dependencies {
        // https://github.com/Meituan-Dianping/walle
        implementation("com.meituan.android.walle:library:1.1.7")
    }
}
