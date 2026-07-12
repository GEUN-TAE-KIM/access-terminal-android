plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.gtkim.mobile_access_control.feature.common"
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
    api(project(":core"))

    // Compose — 화면 모듈들이 transitive 로 받도록 api
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Lifecycle + Hilt-Nav — 화면들이 직접 사용(collectAsState / hiltViewModel / BaseViewModel) → api
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.hilt.navigation.compose)
    // activity-compose 는 NfcReaderModeEffect(LocalActivity) 내부 전용 → implementation (화면이 직접 안 씀).
    // navigation-compose 는 feature 레이어 미사용(:app 이 자체 보유)이라 제거.
    implementation(libs.androidx.activity.compose)

    // MVI — BaseViewModel<UiState, SideEffect, Intent>
    api(libs.orbit.viewmodel)
    api(libs.orbit.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.android)
}
