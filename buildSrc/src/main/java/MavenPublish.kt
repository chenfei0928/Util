import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.getByName
import org.gradle.plugins.signing.SigningExtension

/**
 * https://juejin.cn/post/6953598441817636900
 *
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-12-24 18:22
 */
fun Project.applyMavenPublish() {
    if (hasProperty("android")) {
        val android = extensions.getByName<com.android.build.gradle.LibraryExtension>("android")
        val mainSourceSet: AndroidSourceSet = android.sourceSets.getByName("main")
        // Android libraries
        tasks.create<Jar>("sourcesJar", Jar::class.java, Action<Jar> {
            archiveClassifier.set("sources")
            from(mainSourceSet.java.srcDirs)
        })
        tasks.create("javadoc", Javadoc::class.java, Action<Javadoc> {
            // https://github.com/novoda/bintray-release/issues/71
            exclude("**/*.kt") // < ---- Exclude all kotlin files from javadoc file.
            source(mainSourceSet.java.srcDirs)
            classpath += files(android.bootClasspath.toTypedArray())
            options.encoding = "utf-8"
//            options.charSet = "utf-8"
        })
    } else {
        // Java libraries
        tasks.create<Jar>("sourcesJar", Jar::class.java, Action<Jar> {
            dependsOn(tasks.getByName("classes"))

            archiveClassifier.set("sources")
//            from(sourceSets.main.allSource)
        })
    }

    // 强制 Java/JavaDoc 等的编码为 UTF-8
    tasks.withType(JavaCompile::class.java, Action<JavaCompile> {
        options.encoding = "UTF-8"
    })

    val javadoc: DomainObjectCollection<Javadoc> =
        tasks.withType(Javadoc::class.java, Action<Javadoc> {
            options.encoding = "UTF-8"
        })

    tasks.create("javadocJar", Jar::class.java, Action<Jar> {
        dependsOn(javadoc.toTypedArray())

        archiveClassifier.set("javadoc")
        from(javadoc.first().destinationDir)
    })

    // add javadoc/source jar tasks as artifacts
    artifacts {
        add("archives", tasks.getByName("javadocJar"))
        add("archives", tasks.getByName("sourcesJar"))
    }

//    apply plugin : 'maven'
//    apply plugin : 'signing'


//Properties properties = new Properties()
//properties.load(project.rootProject.file('local.properties').newDataInputStream())
//
//def ossrhUsername = properties.getProperty("ossrhUsername")
//def ossrhPassword = properties.getProperty("ossrhPassword")

//    def PUBLISH_GROUP_ID = publishedGroupId //这里可以不是直接申请时候的groupId只要开头是就可以
//    def PUBLISH_ARTIFACT_ID = artifact
//    def PUBLISH_VERSION = libraryVersion // android.defaultConfig.versionName //这个是直接获取的库gradle里配置好的版本号，不用到处修改版本号，只需要维护一份就可以。

    //签名
    extensions.configure("signing", Action<SigningExtension> {
//         required { gradle.taskGraph.hasTask("uploadArchives") }
        sign(configurations.getByName("archives"))
    })

//    uploadArchives {
//        repositories {
//            mavenDeployer {
//
//                beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }
//
//                repository(url: "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
//                authentication(userName: ossrhUsername, password: ossrhPassword)
//            }
//
//                snapshotRepository(url: "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
//                authentication(userName: ossrhUsername, password: ossrhPassword)
//            }
//
//                pom.groupId = PUBLISH_GROUP_ID
//                pom.artifactId = PUBLISH_ARTIFACT_ID
//                pom.version = PUBLISH_VERSION
//
//                pom.project {
//                    packaging 'aar' //我这里发布的是安卓的包，所有写的aar
//
//                    name libraryName // '发布库的简单名称'
//                            // optionally artifactId can be defined here
//                            description libraryDescription // '发布包的描述'
//                            url siteUrl // '可以写公司官网地址或github个人页面地址'
//
//                            scm {
//                                connection gitUrl // 'scm:替换成项目git地址'
//                                        developerConnection gitUrl // 'scm:替换为git开头的项目地址'
//                                        url siteUrl // '项目首页，可以是github项目的主页'
//                            }
//
//                    licenses {
//                        license {
//                            name licenseName // 'The Apache License, Version 2.0'
//                                    url licenseUrl // 'http://www.apache.org/licenses/LICENSE-2.0.txt'
//                        }
//                    }
//
//                    developers {
//                        developer {
//                            id developerId // '这里填写申请账号时候的全名就可以'
//                                    name developerName // '这里随意填写就可以'
//                                    email developerEmail// '最好是申请账号时用的邮箱'
//                        }
//                    }
//                }
//            }
//        }
//    }
}
