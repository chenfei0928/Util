package io.github.chenfei0928.data

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DynamicFeatureExtension
import com.android.build.api.dsl.LibraryExtension
import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.ProtobufExtension
import com.google.protobuf.gradle.ProtobufPlugin
import com.google.protobuf.gradle.proto
import io.github.chenfei0928.Deps
import io.github.chenfei0928.Env
import io.github.chenfei0928.compiler.hasKotlin
import io.github.chenfei0928.util.api
import io.github.chenfei0928.util.buildSrcAndroid
import io.github.chenfei0928.util.compileOnly
import io.github.chenfei0928.util.defaultConfig
import io.github.chenfei0928.util.implementation
import io.github.chenfei0928.util.packaging
import io.github.chenfei0928.util.sourceSets
import io.github.chenfei0928.util.writeTmpProguardFile
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

// https://github.com/grpc/grpc-java
private const val grpcVersion = Deps.lib.protobuf.grpcVersion

// https://github.com/grpc/grpc-kotlin
private const val grpcKotlinVersion = Deps.lib.protobuf.grpcKotlinVersion

// https://github.com/protocolbuffers/protobuf
// https://github.com/protocolbuffers/protobuf/blob/main/java/lite.md
// https://mvnrepository.com/artifact/com.google.protobuf/protobuf-javalite
private const val protobufVersion = Deps.lib.protobuf.protobufVersion

private const val proguardFileName = "protobuf.pro"
private const val proguardRules = """
# 由buildSrc中Protobuf.kt文件自动生成
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
"""

/**
 * 添加Protobuf依赖
 *
 * [JavaLite](https://github.com/protocolbuffers/protobuf/blob/master/java/lite.md)
 * [GrpcKotlin 接入指南](https://github.com/grpc/grpc-kotlin/blob/master/compiler/README.md)
 *
 * @author ChenFei(chenfei0928@gmail.com)
 * @date 2021-10-28 15:12
 */
fun Project.applyProtobufDependencies(includeGrpc: Boolean = true) {
    when (val ext = buildSrcAndroid<CommonExtension>()) {
        is ApplicationExtension -> {
            ext.packaging.resources.excludes.add("**.proto")
        }
        is LibraryExtension -> {
            ext.packaging.resources.excludes.add("**.proto")
        }
        is DynamicFeatureExtension -> {
            ext.packaging.resources.excludes.add("**.proto")
        }
    }

    dependencies {
        if (includeGrpc) {
            // gRPC
            // https://github.com/grpc/grpc-java
            api("io.grpc:grpc-okhttp:$grpcVersion")
            if (Env.protobufType.useFullDependencies) {
                api("io.grpc:grpc-protobuf:$grpcVersion")
            } else {
                api("io.grpc:grpc-protobuf-lite:$grpcVersion")
            }
            api("io.grpc:grpc-stub:$grpcVersion")
            if (hasKotlin) {
                api("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
            }

            api("io.grpc:grpc-android:$grpcVersion")

            // necessary for Java 9+
            // https://mvnrepository.com/artifact/org.apache.tomcat/annotations-api
            compileOnly("org.apache.tomcat:annotations-api:6.0.53")
        }

        // Protobuf
        implementation("com.google.protobuf:protoc:$protobufVersion")
        if (Env.protobufType.useFullDependencies) {
            api(Deps.lib.protobuf.java)
            api(Deps.lib.protobuf.util)
            if (hasKotlin) {
                api(Deps.lib.protobuf.kotlin)
            }
        } else {
            implementation(Deps.lib.protobuf.kotlin)
            if (hasKotlin) {
                api(Deps.lib.protobuf.kotlinLite)
            }
        }
    }
}

/**
 * 配置Protobuf的plugin
 * Protobuf数据结构自动生成、grpc调用自动生成
 *
 * @param useKotlinExt 是否生成Protobuf数据结构的Kotlin扩展函数
 */
fun Project.applyProtobuf(includeGrpc: Boolean = false, useKotlinExt: Boolean = false) {
    apply<ProtobufPlugin>()

    applyProtobufDependencies(includeGrpc = includeGrpc)

    if (Env.protobufType.includeProguardRule) {
        val proguardFile = writeTmpProguardFile(proguardFileName, proguardRules)

        buildSrcAndroid<CommonExtension>().apply {
            defaultConfig {
                proguardFile(proguardFile)
            }
        }
    }

    buildSrcAndroid<CommonExtension>().apply {
        sourceSets {
            named("main") {
                proto {
                    srcDir("src/main/proto")
                }
            }
        }

        // 打包时排除proto接口的源代码
        packaging {
            resources.excludes.add("**.proto")
        }
    }

    extensions.configure<ProtobufExtension>("protobuf") {
        protoc {
            artifact = "com.google.protobuf:protoc:$protobufVersion"
        }
        plugins {
            create("java") {
                artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
            }
            if (includeGrpc) {
                create("grpc") {
                    artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
                }
                if (hasKotlin) {
                    create("grpckt") {
                        artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
                    }
                }
            }
        }

        generateProtoTasks {
            all().all {
                plugins {
                    create("java") {
                        this.useLite = Env.protobufType.useProtobufLite
                    }
                    if (includeGrpc) {
                        create("grpc") {
                            this.useLite = Env.protobufType.useProtobufLite
                        }
                        if (hasKotlin) {
                            create("grpckt") {
                                this.useLite = Env.protobufType.useProtobufLite
                            }
                        }
                    }
                }
                // 是否生成protobuf实体类扩展函数
                if (useKotlinExt && hasKotlin) {
                    builtins {
                        create("kotlin") {
                            this.useLite = Env.protobufType.useProtobufLite
                        }
                    }
                }
            }
        }
    }
}

private var GenerateProtoTask.PluginOptions.useLite: Boolean
    get() = "lite" in options
    set(value) {
        if (value) {
            option("lite")
        } else {
            options.remove("lite")
        }
    }
