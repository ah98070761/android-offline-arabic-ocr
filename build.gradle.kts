// build.gradle.kts (ملف الجذر)

plugins {
    // يجب تحديد الإصدار هنا (AGP 8.1.0، Kotlin 1.9.0)
    // نستخدم إصدارات مستقرة. إذا كان لديك متطلبات أخرى، قم بالتحديث.
    id("com.android.application") version "8.1.0" apply false 
    id("com.android.library") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}

// لا تضع أي كود آخر لـ Android أو Dependencies في هذا الملف