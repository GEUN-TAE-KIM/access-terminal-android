plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.gtkim.mobile_access_control.feature.register"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":feature:common"))
    implementation(project(":component:scan"))
    implementation(project(":component:auth"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.orbit.test)
    testImplementation(libs.mockk.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
