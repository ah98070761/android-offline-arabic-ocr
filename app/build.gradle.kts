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
        
        // 💡 إضافة: تحديد نسخة NDK لتحسين التوافق مع المكتبات الأصلية
        ndk {
            abiFilters("armeabi-v7a", "arm64-v8a", "x86", "x86_64") 
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
        // تم التحديث إلى Java 17
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        // تم التحديث إلى Java 17
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        compose = false 
    }
    packaging {
        // 💡 إضافة: استخدام وضع التغليف القديم للمكتبات الأصلية (لحل مشاكل التوقف الفوري)
        jniLibs {
             useLegacyPackaging = true
        }
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

    // العودة إلى صيغة التبعية القياسية، والتي ستعمل مع Gradle 8.8
    implementation("com.rmtheis:tess-two:9.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}