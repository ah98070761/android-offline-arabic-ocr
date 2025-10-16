// app/build.gradle.kts

plugins {
    id("com.android.application") 
    id("org.jetbrains.kotlin.android")
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
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        compose = false 
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    
    // لإضافة await() coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") 

    // ✅ التبعية الصحيحة والموثوقة للتعرف على النص (بما في ذلك اللغة العربية)
    // نستخدم play-services-mlkit-text-recognition الذي يعمل عبر خدمات Google Play.
    // الإصدار 19.0.1 هو أحدث إصدار مستقر تم التحقق منه.
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1") 

    // ❌ تم حذف com.google.mlkit:text-recognition و com.google.mlkit:text-recognition-arabic

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}