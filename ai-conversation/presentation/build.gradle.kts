plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.purpletear.aiconversation.presentation"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    buildTypes.getByName("release") {
        isDebuggable = false
    }

    buildTypes.getByName("debug") {
        isDebuggable = true
    }
}

dependencies {

    implementation(project(":ai-conversation:data"))
    implementation(project(":ai-conversation:domain"))
    implementation(project(":ai-conversation:core"))
    implementation(project(":shop:presentation"))
    implementation(project(":shop:domain"))
    implementation(project(":shop:data"))
    implementation(project(":user:domain"))
    implementation(project(":core:presentation"))
    implementation(project(":permission:domain"))
    implementation(project(":notification:domain"))

    implementation(libs.androidx.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.android.material)

    implementation(libs.androidx.media3.ui)
    implementation(project(":shared-elements"))
    implementation(project(":in-app-purchase:domain"))
    implementation(project(":popup:domain"))
    implementation(project(":Framework"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.compose)

    implementation(libs.compose.ui)
    implementation(libs.androidx.ui.graphics)

    implementation(libs.coil.compose)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.constraintlayout.compose)

    implementation(libs.composeshadowsplus)

    implementation(libs.dagger.hilt)
    ksp(libs.dagger.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.work)

    androidTestImplementation(libs.dagger.hilt.testing)
    kspAndroidTest(libs.dagger.hilt.compiler)
    kspAndroidTest(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.hilt.work)
    implementation(libs.lottie.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.google.firebase.messaging.ktx)

    implementation(libs.zoomable)
    implementation(libs.easycrop)

    implementation(libs.androidx.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)

}
