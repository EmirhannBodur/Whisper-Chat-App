plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id ("dagger.hilt.android.plugin")
    id ("kotlin-kapt")
}

android {
    namespace = "com.emirhan.whisper"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.emirhan.whisper"
        minSdk = 24
        targetSdk = 36
        versionCode = 13
        versionName = "6.2"

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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics")

    val nav_version = "2.9.1"

    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.google.dagger:hilt-android:2.56.2")
    kapt ("com.google.dagger:hilt-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")


    // Fotoğraf seçimi ve çekimi için
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("androidx.compose.ui:ui:1.6.0")

// Firebase
    implementation("com.google.firebase:firebase-storage-ktx")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.firebase:firebase-messaging")


    // ExoPlayer (Media3)
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    // Video oynatmak için UI bileşenleri
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
    // DASH protokolü desteği (isteğe bağlı)
    implementation("androidx.media3:media3-exoplayer-hls:1.3.1")
    // HLS protokolü desteği (isteğe bağlı)
    // implementation "androidx.media3:media3-exoplayer-rtsp:1.3.1" // RTSP protokolü desteği (isteğe bağlı)
    // Coil Compose
    implementation("io.coil-kt:coil-compose:2.7.0")
    // En son stabil sürüm
    implementation("androidx.compose.material3:material3:1.2.1")
// Veya kullandığınız en son stabil sürüm
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")
// Veya kullandığınız en son stabil sürüm
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
// Eğer Icons.Extended'dan da ikon kullanıyorsanız
}
kapt {
    correctErrorTypes = true
}