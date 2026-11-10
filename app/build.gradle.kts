plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)   // Xử lý google-services.json
    alias(libs.plugins.ksp)               // Chạy Glide compiler
}

android {
    namespace = "vn.edu.vlu.lab09"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "vn.edu.vlu.lab09"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Bật View Binding: sinh ra ActivityAuthBinding, ActivityMainBinding, ItemPhotoBinding
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    // Firebase BoM quản lý version, KHÔNG ghi version cho firebase-auth-ktx
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)

    // Glide + KSP compiler
    implementation(libs.glide)
    ksp(libs.glide.ksp)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}