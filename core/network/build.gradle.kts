import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    api(project(":core"))

    // public RetrofitFactory.create(baseUrl, client: OkHttpClient, json: Json): Retrofit 시그니처가
    // 이 타입들을 노출 → api (호출자 :app 이 그대로 사용). converter 는 factory 내부 전용이라 implementation.
    api(libs.retrofit.core)
    api(libs.okhttp.core)
    api(libs.kotlinx.serialization.json)
    api(libs.javax.inject) // @Qualifier(AuthApiClient) 가 public
    implementation(libs.retrofit.converter.kotlinx.serialization)

    testImplementation(libs.junit)
    testImplementation(libs.mockk.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
}
