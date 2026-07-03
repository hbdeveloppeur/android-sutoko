plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.purpletear.sutoko.logger.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        val exceptionChannel = project.findProperty("LOGGER_EXCEPTION_CHANNEL") as? String ?: ""
        val logChannel = project.findProperty("LOGGER_LOG_CHANNEL") as? String ?: ""

        buildConfigField("String", "LOGGER_EXCEPTION_CHANNEL", "\"$exceptionChannel\"")
        buildConfigField("String", "LOGGER_LOG_CHANNEL", "\"$logChannel\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
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

    buildTypes.getByName("release") {
        isDebuggable = false
    }

    buildTypes.getByName("debug") {
        isDebuggable = true
    }
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.ktx)
    implementation(libs.okhttp3)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.dagger.hilt)
    ksp(libs.dagger.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
