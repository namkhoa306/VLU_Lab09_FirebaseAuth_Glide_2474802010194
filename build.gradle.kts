// Top-level build file: khai báo plugin cho cả project, "apply false" = chỉ nạp version
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false   // Đọc google-services.json
    alias(libs.plugins.ksp) apply false               // Annotation processor cho Glide
}