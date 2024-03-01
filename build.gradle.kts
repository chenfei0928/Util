// Top-level build file where you can add configuration options common to all sub-projects/modules.
io.github.chenfei0928.Env.reload(gradle)

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
