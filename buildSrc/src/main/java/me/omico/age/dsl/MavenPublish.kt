@file:Suppress("unused")

package me.omico.age.dsl

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

fun Project.configureMavenPublication(
    mavenPublicationName: String = "maven",
    versionName: String = version.toString(),
    signed: Boolean = true,
    block: MavenPublication.() -> Unit,
) {
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "org.gradle.signing")
    afterEvaluate {
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>(mavenPublicationName) {
                    groupId = getProperty<String>("POM_GROUP_ID")
                    artifactId = getProperty<String>("POM_ARTIFACT_ID")
                    version = versionName
                    pom {
                        name.set(getProperty<String>("POM_NAME"))
                        description.set(getProperty<String>("POM_DESCRIPTION"))
                        url.set(getProperty<String>("POM_URL"))
                        licenses {
                            license {
                                name.set(getProperty<String>("POM_LICENCE_NAME"))
                                url.set(getProperty<String>("POM_LICENCE_URL"))
                            }
                        }
                        developers {
                            developer {
                                id.set(getProperty<String>("POM_DEVELOPER_ID"))
                                name.set(getProperty<String>("POM_DEVELOPER_NAME"))
                            }
                        }
                        scm {
                            connection.set(getProperty<String>("POM_SCM_CONNECTION"))
                            developerConnection.set(getProperty<String>("POM_SCM_DEV_CONNECTION"))
                            url.set(getProperty<String>("POM_SCM_URL"))
                        }
                    }
                    block()
                }
            }
            repositories {
                maven {
                    credentials {
                        username = localProperties.getProperty("NEXUS_USERNAME")
                        password = localProperties.getProperty("NEXUS_PASSWORD")
                    }
                    val name = when {
                        isSnapshot(versionName) -> "NEXUS_PUBLISH_SNAPSHOT_URL"
                        else -> "NEXUS_PUBLISH_RELEASE_URL"
                    }
                    setUrl(localProperties.getProperty(name))
                }
            }
            if (signed && !isSnapshot(versionName)) {
                configure<SigningExtension> {
                    useGpgCmd()
                    sign(publications[mavenPublicationName])
                }
            }
        }
    }
}

fun Project.withKotlinMavenPublication(
    mavenPublicationName: String = "maven",
    versionName: String = version.toString(),
    signed: Boolean = true,
) {
    withJavaSourcesJar()
    configureMavenPublication(
        mavenPublicationName = mavenPublicationName,
        versionName = versionName,
        signed = signed,
    ) {
        from(components["kotlin"])
        artifact(tasks["sourcesJar"])
    }
}

@Suppress("UnstableApiUsage")
fun Project.withKotlinAndroidMavenPublication(
    mavenPublicationName: String = "maven",
    versionName: String = version.toString(),
    signed: Boolean = true,
    componentName: String = "release",
) {
    withAndroidSourcesJar()
    withAndroidLibrary {
        configure<LibraryExtension> {
            publishing {
                singleVariant(componentName)
            }
        }
    }
    configureMavenPublication(
        mavenPublicationName = mavenPublicationName,
        versionName = versionName,
        signed = signed,
    ) {
        from(components[componentName])
        artifact(tasks["androidSourcesJar"])
    }
}

private fun isSnapshot(versionName: String): Boolean = versionName.endsWith("SNAPSHOT")

private inline fun <reified T> Project.getProperty(name: String): T = property(name) as T
