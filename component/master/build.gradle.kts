plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.gtkim.mobile_access_control.component.master"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.work)
    ksp(libs.hilt.ext.compiler)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.mockk.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
