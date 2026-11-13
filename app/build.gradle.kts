plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "com.example"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.example.lumo"
    minSdk = 24
    targetSdk = 34
    versionCode = 12
    versionName = "2.6.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
}

val debugApkFile = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")
val releaseApkFile = layout.buildDirectory.file("outputs/apk/release/app-release.apk")
val rootLumoApk = rootProject.layout.projectDirectory.file("LUMO-v2.6.0.apk").asFile

tasks.register("syncRootApk") {
  dependsOn("assembleDebug")
  outputs.upToDateWhen { false }
  doLast {
    val builtApk = if (releaseApkFile.get().asFile.exists()) releaseApkFile.get().asFile else debugApkFile.get().asFile
    if (builtApk.exists()) {
      builtApk.copyTo(rootLumoApk, overwrite = true)
    }
  }
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
  finalizedBy("syncRootApk")
}
