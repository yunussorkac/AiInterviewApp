plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.serialization)
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.yeslab.interviewapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yeslab.interviewapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.kotlinx.serialization.json)
    implementation (libs.androidx.datastore.preferences)
    implementation (libs.gson)
    implementation (libs.androidx.navigation.compose)

    implementation (libs.koin.androidx.compose)
    implementation (libs.koin.android)
    implementation (libs.koin.androidx.navigation)
    testImplementation(libs.koin.test.junit4)
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.kotlinx.coroutines.play.services)

    implementation (libs.accompanist.systemuicontroller)
    implementation(libs.androidx.runtime.livedata)
    implementation (libs.coil)
    implementation (libs.coil.compose)
    implementation (libs.coil.network.okhttp)

    implementation (libs.retrofit)
    implementation (libs.converter.scalars)
    implementation (libs.converter.gson)

    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.ktx)
    annotationProcessor (libs.androidx.room.compiler)
    ksp (libs.androidx.room.compiler)

    implementation("androidx.compose.material:material-icons-extended:$2024.05.00")



}