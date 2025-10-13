@file:Suppress("DEPRECATION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
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

    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
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
    }

    buildFeatures {
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
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    implementation(libs.exoplayer.ui)
    implementation(libs.exoplayer.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.insert.koin.koin.android) // або новішу версію
    implementation(libs.koin.core)
    implementation(project(":core"))
    implementation(project(":player"))
    implementation(project(":ui"))
    implementation(libs.androidx.material3)
    implementation(libs.coil.kt.coil)
    implementation(libs.io.coil.kt.coil.gif)
    implementation(libs.io.coil.kt.coil.svg)
    testImplementation(libs.mockk.mockk)
    testImplementation(libs.mockk.agent.jvm)
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.leanback)

    // UI/Media
    implementation(libs.glide)
    implementation(libs.exoplayer.hls)

    // Firebase (через BoM)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.google.firebase.config.ktx)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.fragment.testing)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.mockk)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestImplementation(libs.androidx.espresso.core.v361)
    androidTestImplementation(libs.androidx.runner)
    //noinspection GradleDependency
    androidTestImplementation(libs.androidx.rules)
    testImplementation(libs.robolectric.v4122)
    testRuntimeOnly(libs.junit.jupiter.api)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

afterEvaluate {
    tasks.matching { it.name.contains("processPro") && it.name.contains("GoogleServices") }
        .configureEach { enabled = false }
}

afterEvaluate {
    tasks.matching { it.name.contains("uploadCrashlyticsMappingFileProRelease") }
        .configureEach { enabled = false }

    tasks.matching { it.name.contains("processProReleaseGoogleServices") }
        .configureEach { enabled = false }
}
