plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.android.library) apply false
}

tasks.register("cleanAll", Delete::class) {
    delete(rootProject.buildDir)
}

abstract class PrintFlavorTask : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val flavor: Property<String>

    @TaskAction
    fun print() {
        println(">>> Active flavor: ${flavor.orNull ?: "default"}")
    }
}

tasks.register<PrintFlavorTask>("printFlavor") {
    flavor.set(providers.gradleProperty("FLAVOR").orElse("default"))
}

