// app/build.gradle.kts (التغييرات الرئيسية في defaultConfig و sourceSets)

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
        
        // **إزالة كتلة ndk بالكامل**
        // ndk {
        //     abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        // }
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
        // ✅ الإبقاء على حل مشكلة Android 9.0 (مهم)
        jniLibs {
             useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // 🌟 الخطوة الجديدة: ربط jniLibs بالمكتبات الأصلية لـ tess-two
    sourceSets.getByName("main") {
        jniLibs.srcDirs("src/main/jniLibs", 
                       "libs") // أضف المسارات حيث قد تكون tess-two تضع مكتباتها الأصلية
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation("com.rmtheis:tess-two:9.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}