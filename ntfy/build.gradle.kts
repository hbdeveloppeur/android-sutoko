plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt")
    `maven-publish`
}

android {
    namespace = "com.purpletear.ntfy"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // BuildConfig fields for channel IDs (to be set by the consuming app)
        val errorChannel = project.findProperty("NTFY_ERROR_CHANNEL") ?: ""
        val logChannel = project.findProperty("NTFY_LOG_CHANNEL") ?: ""
        val urgentChannel = project.findProperty("NTFY_URGENT_CHANNEL") ?: ""
        
        buildConfigField("String", "NTFY_ERROR_CHANNEL", "\"$errorChannel\"")
        buildConfigField("String", "NTFY_LOG_CHANNEL", "\"$logChannel\"")
        buildConfigField("String", "NTFY_URGENT_CHANNEL", "\"$urgentChannel\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlin {
        jvmToolchain(17)
    }
    
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.ktx)
    implementation(libs.okhttp3)
    implementation(libs.dagger.hilt)
    kapt(libs.dagger.hilt.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Publishing configuration
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                
                groupId = "com.purpletear"
                artifactId = "ntfy"
                version = "1.0.0"
            }
        }
    }
}