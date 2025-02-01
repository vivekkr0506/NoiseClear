plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias (libs.plugins.hilt)
    id("org.jetbrains.kotlin.kapt")
    id ("io.sentry.android.gradle") version "5.0.0"
}

android {
    namespace = "com.noiseclear"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.noiseclear"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            keyAlias = "noiseclear"
            keyPassword = "Vivek@1995"
            storeFile = file("/Users/vivekkumar/AndroidStudioProjects/NoiseClear/noiseclear.jks")
            storePassword = "Vivek@1995"
        }
    }
    buildTypes {
        debug {
            isShrinkResources = false
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }

    testOptions {
       // unitTests.includeAndroidResources = true
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation (libs.mockito.core)
    testImplementation (libs.mockito.android)
    testImplementation (libs.androidx.hilt.lifecycle.viewmodel.testing)
    testImplementation (libs.kotlinx.coroutines.test)
    //Dot Lottie
    implementation(libs.dotlottie.android)
    implementation(libs.lottie.compose)

    implementation (libs.androidx.media3.exoplayer)
    implementation (libs.androidx.media3.ui)
    implementation (libs.androidx.media3.common)

    implementation (libs.androidx.lifecycle.viewmodel.compose)
    implementation (platform(libs.firebase.bom))

    implementation (libs.firebase.analytics.ktx)
    implementation(libs.accompanist.permissions)

    implementation(libs.android.wave.recorder)

    implementation (libs.hilt.navigation.compose)
    implementation (libs.hilt.android)

    kapt(libs.hilt.compiler)

}