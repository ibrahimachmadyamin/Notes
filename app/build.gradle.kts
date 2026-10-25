plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Signing key (opsional) diambil dari environment variable / GitHub Secrets.
// Jika tidak ada, Gradle memakai debug key bawaan (berubah tiap build CI).
val keystorePath: String? = System.getenv("SIGNING_KEYSTORE_PATH")
val hasCustomKey = !keystorePath.isNullOrBlank() && file(keystorePath).exists()

android {
    namespace = "com.baim.catatankontrak"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.baim.catatankontrak"
        minSdk = 26
        targetSdk = 34
        versionCode = (System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1)
        versionName = "1.0.${System.getenv("GITHUB_RUN_NUMBER") ?: "0"}"
    }

    signingConfigs {
        if (hasCustomKey) {
            create("stable") {
                storeFile = file(keystorePath!!)
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            if (hasCustomKey) signingConfig = signingConfigs.getByName("stable")
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = if (hasCustomKey) signingConfigs.getByName("stable")
            else signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")

    debugImplementation("androidx.compose.ui:ui-tooling")
}

