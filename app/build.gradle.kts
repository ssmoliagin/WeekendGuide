plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services") // 🔥 Firebase integration
    kotlin("plugin.serialization") version "1.9.23"
}

android {
    namespace = "com.example.weekendguide"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.weekendguide"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.2.90"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = project.findProperty("GOOGLE_MAPS_API_KEY") as? String ?: ""
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
        buildConfig = true
        compose = true
    }
}

dependencies {
    // 🔷 Base AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.appcompat:appcompat:1.7.1")

    // 🧱 Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-util")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-graphics-android")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.graphics)

    // 🧪 Compose Preview / Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // 🧪 Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // 🔥 Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-messaging")
    implementation(libs.firebase.storage.ktx)

    // 🔐 Google Sign-In & Location
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.libraries.places:places:4.3.1")

    // 🗺 Google Maps Compose
    implementation("com.google.maps.android:maps-compose:2.11.4")
    implementation("com.google.maps.android:android-maps-utils:3.8.0")

    // 🧭 Navigation
    implementation("androidx.navigation:navigation-compose:2.9.1")

    // 🧠 ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.1")

    // ⚙️ DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // ☁️ Networking & JSON
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // 🔄 Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

    // 🖼 Image loading
    implementation("io.coil-kt:coil-compose:2.4.0")
}
