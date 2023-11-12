plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    kotlin("android")
}

android {
    namespace = "com.unomaster.pokedexgame.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.unomaster.pokedexgame.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:30.0.1"))
    implementation(project(":shared"))

    implementation("com.google.firebase:firebase-common-ktx:20.4.2")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-tooling:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation("androidx.compose.material:material:1.5.4")
    implementation("androidx.activity:activity-compose:1.8.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

}