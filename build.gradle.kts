// build.gradle.kts (ملف الجذر)

plugins {
    // متوافق مع compileSdk 34
    id("com.android.application") version "8.3.0" apply false
    id("com.android.library") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}

buildscript {
    repositories {
        google()        // ✅ ضروري لمكتبات ML Kit
        mavenCentral()  // ✅ ضروري لمكتبات Kotlin وRoom وغيرها
    }
    dependencies {
        // يمكن تركها فارغة لأن AGP محدث بالفعل
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}