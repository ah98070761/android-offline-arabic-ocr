// build.gradle.kts (ملف الجذر)

plugins {
    // يجب تحديد الإصدار هنا
    id("com.android.application") version "8.1.0" apply false 
    id("com.android.library") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}

// لا تضع أي تهيئة لـ android { ... } أو dependencies { ... } هنا.