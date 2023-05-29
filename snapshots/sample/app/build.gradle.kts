plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.emergetools.snapshots.sample"
  compileSdk = 33

  defaultConfig {
    applicationId = "com.emergetools.snapshots.sample"
    minSdk = 26
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
      sourceSets {
        getByName("androidTest") {
          java.srcDir("generated/ksp/debugAndroidTest/kotlin")
        }
      }
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
  }

  composeOptions {
    kotlinCompilerExtensionVersion = "1.4.7"
  }
}

dependencies {
  implementation(libs.androidx.activity.activity)
  implementation(libs.androidx.activity.compose)

  implementation(projects.snapshots.sample.uiModule)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.tooling)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.compose.material)

  kspAndroidTest(projects.snapshots.snapshotsProcessor)

  debugImplementation(projects.snapshots.sample.uiModule)
  androidTestImplementation(projects.snapshots.snapshots)
  androidTestImplementation(libs.compose.runtime)
  androidTestImplementation(libs.junit)
}
