plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // إضافة الإضافات المطلوبة لـ Room و Kotlin (kapt)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.ocr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ocr"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    // تفعيل ViewBinding للاستخدام في MainActivity
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    // Core Android Libraries
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // ML Kit for Arabic Text Recognition (Unbundled)
    // التبعية الخاصة باللغة العربية
    implementation("com.google.mlkit:text-recognition-arabic:16.0.0") 

    // ML Kit Tasks-Ktx for Coroutine support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0") 

    // 1. Room - Database for Saving Extracted Text
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    // لتوليد الكود الخاص بـ Room (KAPT)
    kapt("androidx.room:room-compiler:$room_version")
    // دعم Coroutines لـ Room
    implementation("androidx.room:room-ktx:$room_version")
    
    // 2. Google Mobile Ads SDK (AdMob)
    // إضافة مكتبة إعلانات Google Mobile
    implementation("com.google.android.gms:play-services-ads:23.1.0") 
    
    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
