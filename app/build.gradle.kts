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

    // 🔧 Deixe Java e Kotlin em 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.3" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

// 🔧 Garante o jvmTarget do Kotlin em 17
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
}
