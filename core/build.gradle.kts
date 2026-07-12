import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
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
    // Coroutines (pure JVM artifact)
    api(libs.kotlinx.coroutines.core)

    // Qualifier annotations (javax.inject) — pure Java, no Hilt
    api(libs.javax.inject)

    // androidx.annotation
    api(libs.androidx.annotation)

    testImplementation(libs.junit)
    testImplementation(libs.mockk.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
