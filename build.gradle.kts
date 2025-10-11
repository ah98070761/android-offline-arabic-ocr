// build.gradle.kts (ملف الجذر)

plugins {
    // تحديث AGP إلى 8.3.0 ليتوافق تمامًا مع compileSdk 34
    id("com.android.application") version "8.3.0" apply false 
    id("com.android.library") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}