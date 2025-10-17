plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-kapt'
}

android {
    namespace 'com.example.ocr'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.ocr"
        minSdk 21
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
        debug {
            // Debug-specific config
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding true
        dataBinding true
    }
}

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.0"

    // AndroidX
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.2.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.2'
    implementation 'androidx.activity:activity-ktx:1.9.0'

    // Room Database
    implementation 'androidx.room:room-runtime:2.6.2'
    kapt 'androidx.room:room-compiler:2.6.2'
    implementation 'androidx.room:room-ktx:2.6.2'

    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'

    // ML Kit Text Recognition Arabic
    implementation 'com.google.mlkit:text-recognition-arabic:17.1.0' 

    // PDF Renderer (مضمنة في Android SDK)
    // no additional dependency needed

    // AdMob
    implementation 'com.google.android.gms:play-services-ads:22.3.0'

    // Logging
    implementation 'androidx.logging:logging:1.0.0'

    // Testing (optional)
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}