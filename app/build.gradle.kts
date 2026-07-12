plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.gtkim.mobile_access_control"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.gtkim.mobile_access_control"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BASE_URL 은 buildType 별로 분리 (debug=로컬, release=placeholder). 양쪽 공통 필드만 여기.
        buildConfigField("String", "TERMINAL_ID", "\"DEV-TERMINAL-001\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://localhost:8080/\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // multi-module structure
    implementation(project(":feature:common"))
    implementation(project(":feature:login"))
    implementation(project(":feature:scan"))
    implementation(project(":feature:register"))
    implementation(project(":feature:history"))
    implementation(project(":feature:stats"))
    implementation(project(":core"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":component:auth"))
    implementation(project(":component:access"))
    implementation(project(":component:nfc"))
    implementation(project(":component:master"))
    implementation(project(":component:sync"))
    implementation(project(":component:history"))
    implementation(project(":component:stats"))

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // WorkManager (Bootstrapper enqueues, factory provided here)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.ext.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Logging
    implementation(libs.timber)

    // Serialization (type-safe Nav routes)
    implementation(libs.kotlinx.serialization.json)

    // OkHttp logging (for NetworkModule HttpLoggingInterceptor)
    implementation(libs.okhttp.logging)

    // Test
    testImplementation(libs.junit)
    testImplementation(libs.orbit.test)
    testImplementation(libs.mockk.core)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
