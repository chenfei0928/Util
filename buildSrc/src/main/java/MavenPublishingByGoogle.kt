import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.extra
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
fun Project.applyMavenPublishByGoogle(
    groupId: String, artifactId: String, version: String,
    description: String, username: String, gitPageUrl: String, inception: String
) {
    val properties: Map<Any, Any> = project.rootProject.file("local.properties")
        .reader().use {
            java.util.Properties().apply {
                load(it)
            }
        }

    // Because the components are created only during the afterEvaluate phase, you must
    // configure your publications using the afterEvaluate() lifecycle method.
    afterEvaluate {
        extensions.configure("publishing", Action<PublishingExtension> {
            publications {
                create("release", MavenPublication::class.java, Action<MavenPublication> {
                    this.groupId = groupId
                    this.artifactId = artifactId
                    this.version = version

//                    artifact(javadocJar.get())
                    from(components["release"])

                    pom {
                        // Description
                        this.description.set(description)
                        url.set(gitPageUrl)

                        // License
                        inceptionYear.set(inception)
                        licenses {
                            license {
                                name.set("The Apache Software License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                name.set(username)
                            }
                        }
                        scm {
                            connection.set("$gitPageUrl.git")
                            developerConnection.set("$gitPageUrl.git")
                            url.set("$gitPageUrl/issues")
                        }
                    }
                })
            }

            // Configure MavenCentral repository
            repositories {
                maven {
                    name = "sonatype"
                    setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        this.username = properties["ossrh.username"].toString()
                        this.password = properties["ossrh.password"].toString()
                    }
                }
            }

            // Configure MavenLocal repository
            repositories {
                maven {
                    name = "myMavenlocal"
                    url = uri(java.lang.System.getProperty("user.home") + "/.m2/repository")
                }
            }
        })

        extensions.configure("signing", Action<org.gradle.plugins.signing.SigningExtension> {
            arrayOf(
                "signing.keyId", "signing.password", "signing.secretKeyRingFile"
            ).forEach { key ->
                this@applyMavenPublishByGoogle.extra[key] = properties[key]
            }

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
