plugins {
    // This project was created with AGP 9.x, which includes built-in Kotlin support.
    // Do not add org.jetbrains.kotlin.android on top of AGP 9 built-in Kotlin.
    alias(libs.plugins.android.application)
}

val configuredApiBaseUrl = providers.gradleProperty("NBK_API_BASE_URL").orElse("").get()
    .replace("\\", "\\\\").replace("\"", "\\\"")
val configuredGoogleWebClientId = providers.gradleProperty("NBK_GOOGLE_WEB_CLIENT_ID").orElse("").get()
    .replace("\\", "\\\\").replace("\"", "\\\"")

android {
    namespace = "com.example.nbk_travels"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.nbk_travels"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"$configuredApiBaseUrl\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$configuredGoogleWebClientId\"")
    }

    buildFeatures {
        buildConfig = true
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")

    // Google SSO uses Credential Manager + Google ID tokens.
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
