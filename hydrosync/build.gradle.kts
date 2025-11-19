// root build.gradle.kts
plugins {
    // Keep plugin versions consistent here
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.dagger.hilt.android") version "2.47" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
