plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
}

val keystoreFile = rootProject.file("keystore.properties")
val keystoreExists = keystoreFile.exists()
val keystoreProps = mutableMapOf<String, String>()
if (keystoreExists) {
    keystoreFile.readLines().forEach { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
            val eq = trimmed.indexOf('=')
            if (eq > 0) {
                keystoreProps[trimmed.substring(0, eq).trim()] = trimmed.substring(eq + 1).trim()
            }
        }
    }
}

android {
    namespace = "com.example.torque"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.torque"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SENTRY_DSN", "\"https://example@sentry.io/placeholder\"")
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreProps["storeFile"]?.let { rootProject.file(it) }
            storePassword = keystoreProps["storePassword"]
            keyAlias = keystoreProps["keyAlias"]
            keyPassword = keystoreProps["keyPassword"]
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

detekt {
    config.setFrom(files("${rootDir}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
}

dependencies {
    // --- Módulos internos ---
    implementation(project(":core"))
    implementation(project(":data"))

    // --- BOM de Compose ---
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // --- UI, Material 3 y Navegación ---
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // --- Componentes Base de Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    // --- Persistencia de Datos (DataStore y Room) ---
    implementation(libs.androidx.datastore.preferences)

    // Room - usando KSP
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- Imágenes (Coil) ---
    implementation(libs.coil.compose)

    // --- DI (Koin) ---
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // --- Error Tracking (Sentry) ---
    implementation(libs.sentry.android)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
