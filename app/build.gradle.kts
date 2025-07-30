
plugins {
    alias(libs.plugins.androidApplication) // Assuming you aliased it as such in libs.versions.toml
    alias(libs.plugins.kotlinAndroid)     // Assuming you aliased it
    alias(libs.plugins.kotlinCompose)
}

android {
    namespace = "com.example.alarm_o_clock"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.alarm_o_clock"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core AndroidX libraries
    implementation(libs.androidx.core.ktx) // Keep one, ensure it's the correct alias from your libs.versions.toml
    implementation(libs.androidx.lifecycle.runtime.ktx) // Keep one
    implementation(libs.androidx.activity.compose)   // Keep one
    implementation("com.google.android.material:material:1.12.0")

    // --- Compose Dependencies ---
    // 1. Declare the Compose BOM (Bill of Materials) - CHOOSE ONE BOM
    //    Make sure 'libs.androidx.compose.bom' in your libs.versions.toml points to the latest stable BOM version
    //    e.g., in libs.versions.toml: androidx-compose-bom = "2024.02.01" (check for the latest)
    implementation(platform(libs.androidx.compose.bom)) // This should be your primary BOM

    // 2. Compose UI libraries (no versions needed as they are managed by the BOM)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.ui:ui-tooling:1.8.3") // Use the latest stable or desired version
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.androidx.navigation.runtime.android) // Or the latest version

    // Debug implementations for Compose
    debugImplementation("androidx.compose.ui:ui-tooling-preview:1.8.3") // For previews
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Use the same BOM for androidTest Compose dependencies
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

}
