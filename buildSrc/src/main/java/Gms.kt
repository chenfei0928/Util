import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * https://developers.google.com/android/guides/opensource
 * classpath deps.gms.oss.plugin
 *
 * plugins {
 *     id('com.google.android.gms.oss-licenses-plugin')
 * }
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-08-01 11:14
 */
fun Project.applyGms() {
    dependencies {
        // 开源通知
        implementation(DepsGms.oss.core)
    }
}
