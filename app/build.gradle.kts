@file:Suppress("DEPRECATION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21"
}

android {
    namespace = "com.vs18.tvdemoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.vs18.tvdemoapp"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "type"

    productFlavors {
        create("free") {
            dimension = "type"
            applicationIdSuffix = ".free"
            versionNameSuffix = "-free"
            resValue("string", "app_name", "TvDemoApp Free")
        }
        create("pro") {
            dimension = "type"
            applicationIdSuffix = ".pro"
            versionNameSuffix = "-pro"
            resValue("string", "app_name", "TvDemoApp Pro")
        }
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    signingConfigs {
        val releaseStorePassword = System.getenv("SIGNING_STORE_PASSWORD")
            ?: project.findProperty("SIGNING_STORE_PASSWORD")?.toString()
        val releaseKeyAlias = System.getenv("SIGNING_KEY_ALIAS")
            ?: project.findProperty("SIGNING_KEY_ALIAS")?.toString()
        val releaseKeyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            ?: project.findProperty("SIGNING_KEY_PASSWORD")?.toString()

        if (releaseStorePassword == null || releaseKeyAlias == null || releaseKeyPassword == null) {
            println("⚠️ WARNING: Missing signing credentials. Using debug keystore instead.")
        }

        create("release") {
            if (releaseStorePassword != null && releaseKeyAlias != null && releaseKeyPassword != null) {
                storeFile = file(project.findProperty("SIGNING_STORE_FILE")?.toString() ?: "keystore.jks")
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            } else {
                // fallback to debug keystore if secrets missing
                storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/INDEX.LIST"
            )
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

/**
 * === Custom Tasks ===
 * Автоматична збірка і підпис AAB для free/pro flavor
 */
tasks.register("buildAndSignAABFree") {
    dependsOn("bundleFreeRelease")
    doLast {
        println("✅ Building AAB for Free flavor...")
        project.copy {
            from("build/outputs/bundle/freeRelease/app-free-release.aab")
            into("build/outputs/bundle/custom/free")
        }
    }
}

tasks.register("buildAndSignAABPro") {
    dependsOn("bundleProRelease")
    doLast {
        println("✅ Building AAB for Pro flavor...")
        project.copy {
            from("build/outputs/bundle/proRelease/app-pro-release.aab")
            into("build/outputs/bundle/custom/pro")
        }
    }
}

dependencies {
    implementation(libs.extension.ima)
    implementation(libs.translate)
    implementation(libs.androidx.espresso.core.v361)
    implementation(libs.androidx.security.crypto)
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.ui)

    // Modules
    implementation(project(":core"))
    implementation(project(":player"))
    implementation(project(":ui"))

    // Media / TV
    implementation(libs.exoplayer.core)
    implementation(libs.exoplayer.ui)
    implementation(libs.exoplayer.hls)
    implementation(libs.androidx.leanback)

    // Dependency Injection
    implementation(libs.insert.koin.koin.android)
    implementation(libs.koin.core)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.crashlytics.ndk)

    // Image Loading
    implementation(libs.coil.kt.coil)
    implementation(libs.io.coil.kt.coil.gif)
    implementation(libs.io.coil.kt.coil.svg)
    implementation(libs.glide)

    // Google Play API
    implementation(libs.apis.google.api.services.androidpublisher)
    implementation(libs.google.api.services.androidpublisher.vv3rev20250904200)
    implementation(libs.http.client.google.http.client)
    implementation(libs.core.ktx)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.espresso.idling.resource)
    implementation(libs.integrity)
    implementation(libs.androidx.ui.test.junit4)
    implementation(libs.androidx.uiautomator)
    implementation(project(":ml"))

    // Testing
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.agent.jvm)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.robolectric.v4122)
    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.espresso.core.v361)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.fragment.testing)
    androidTestImplementation(libs.androidx.junit.ktx)
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4.v170beta02)
    testImplementation("androidx.compose.ui:ui-test-junit4:1.9.4")
    testImplementation(libs.robolectric.v4111)
}

afterEvaluate {
    // Вимикаємо GoogleServices для pro flavor (немає json-ключа)
    tasks.matching { it.name.contains("processPro") && it.name.contains("GoogleServices") }
        .configureEach { enabled = false }

    tasks.matching { it.name.contains("uploadCrashlyticsMappingFileProRelease") }
        .configureEach { enabled = false }

    tasks.matching { it.name.contains("processProReleaseGoogleServices") }
        .configureEach { enabled = false }
}
