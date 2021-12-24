import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register

/**
 * [GoogleDocs](https://developer.android.com/studio/build/maven-publish-plugin#kts)
 * [GradleDocs](https://docs.gradle.org/current/userguide/publishing_maven.html)
 * https://2bab.me/2021/05/09/trap-of-maven-central-publish
 * https://juejin.cn/post/6953598441817636900
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-12-09 17:12
 */
fun Project.applyMavenPublish() {
    // Because the components are created only during the afterEvaluate phase, you must
    // configure your publications using the afterEvaluate() lifecycle method.
    afterEvaluate {
        extensions.configure("publishing", Action<PublishingExtension> {
            publications {
                create("release", MavenPublication::class.java, Action<MavenPublication> {
                    groupId = "io.github.chenfei0928"
                    artifactId = "util"
                    version = "1.0"

                    from(components["release"])
                })
            }
        })

        extensions.configure("signing", Action<org.gradle.plugins.signing.SigningExtension> {
            val publishing = extensions.getByName("publishing") as PublishingExtension
            sign(publishing.publications["release"])
        })

        tasks.register<org.gradle.api.tasks.javadoc.Javadoc>("javadoc") {
            if (JavaVersion.current().isJava9Compatible) {
                (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
            }
        }
    }
}
