import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

// Apply Google Services plugin only if google-services.json exists
val googleServicesFile = rootProject.file("app/google-services.json")
if (googleServicesFile.exists()) {
  apply(plugin = "com.google.gms.google-services")
}

val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
  localProps.load(localPropsFile.inputStream())
}

android {
    namespace = "com.core2studio.mymanager"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.core2studio.mymanager"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${localProps.getProperty("CLOUDINARY_CLOUD_NAME", "")}\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"${localProps.getProperty("CLOUDINARY_UPLOAD_PRESET", "")}\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("mymanager-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = "mymanager"
            keyPassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = true
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
        excludes += "META-INF/DEPENDENCIES"
        excludes += "META-INF/INDEX.LIST"
      }
    }
}

// Force all gRPC modules to the same version to avoid
// NoClassDefFoundError for InternalGlobalInterceptors.
// google-http-client pulls grpc-context:1.68.2 which upgrades
// grpc-api past what Firestore's gRPC 1.62.2 stack expects.
configurations.all {
  resolutionStrategy {
    eachDependency {
      if (requested.group == "io.grpc") {
        useVersion("1.62.2")
      }
    }
  }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Room
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)

  // Navigation Compose
  implementation(libs.navigation.compose)

  // Coil (Image Loading)
  implementation(libs.coil.compose)
  implementation(libs.coil.network.okhttp)

  // Kotlinx Serialization
  implementation(libs.kotlinx.serialization.json)

  // Google Identity / Credential Manager
  implementation(libs.google.identity)
  implementation(libs.credentials)
  implementation(libs.credentials.play.services)
  implementation(libs.google.id)

  // Firebase
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
}
