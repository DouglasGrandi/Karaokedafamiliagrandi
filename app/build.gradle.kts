plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "net.natura.karaokedafamiliagrandi"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.natura.karaokedafamiliagrandi"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    // Alinhar Java e Kotlin em 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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

    // Jetpack Compose
    buildFeatures {
        compose = true
    }
    // ✅ Compose Compiler compatível com Kotlin 1.9.24
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    // Kotlin DSL correto
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Você pode manter 1.6.x; aqui sugiro 1.6.8 que é estável
    implementation("androidx.compose.ui:ui:1.6.8")
    implementation("androidx.compose.material3:material3:1.2.1")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.leanback:leanback:1.1.0-rc02")

    // Media3 (ExoPlayer) para vídeos locais
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
}
