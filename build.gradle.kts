// Top-level build file where you can add configuration options common to all sub-projects/modules.
Env.reload(gradle.startParameter)

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
