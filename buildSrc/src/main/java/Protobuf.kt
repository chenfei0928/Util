import com.google.protobuf.gradle.*
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/// https://github.com/grpc/grpc-java
const val grpcVersion = "1.40.2"

// https://github.com/protocolbuffers/protobuf
const val protobufVersion = "3.19.1"

/**
 * @author chenfei(chenfei0928@gmail.com)
 * @date 2021-10-28 15:12
 */
fun Project.applyProtobuf() {
    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:$protobufVersion"
        }
        plugins {
            create("grpc") {
                artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
            }
//            create("javalite", Action {
//                artifact = "com.google.protobuf:protoc-gen-javalite:3.0.0"
//            })
        }
//    generateProtoTasks {
//        all()*.plugins {
//            javalite {}
//        }
//        ofNonTest()*.plugins {
//            grpc {
//                // Options added to --grpc_out
//                option 'lite'
//            }
//        }
//    }
        generateProtoTasks {
            all().all {
                builtins {
                    create("java") {
                        option("lite")
                    }
                }
                plugins {
                    create("grpc") {
                        // Options added to --grpc_out
                        option("lite")
                    }
                }
            }
        }
//    generateProtoTasks {
//        all().each { task ->
//            task.builtins {
//                remove java
//            }
//            task.builtins {
//                java {}// 生产java源码
//            }
//        }
//    }
    }

    dependencies {
        // gRPC
        // https://github.com/grpc/grpc-java
        implementation("io.grpc:grpc-okhttp:$grpcVersion")
        implementation("io.grpc:grpc-protobuf:$grpcVersion")
//        implementation("io.grpc:grpc-protobuf-lite:$grpcVersion")
        implementation("io.grpc:grpc-stub:$grpcVersion")

        implementation("io.grpc:grpc-android:$grpcVersion")

        // Protobuf
        implementation("com.google.protobuf:protoc:$protobufVersion")
        implementation("com.google.protobuf:protobuf-java:$protobufVersion")
//        implementation("com.google.protobuf:protobuf-javalite:$protobufVersion")
    }
}
