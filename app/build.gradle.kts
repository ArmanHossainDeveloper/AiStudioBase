import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
}

android {
  namespace = "app.test"
  compileSdk = 34

  defaultConfig {
    applicationId = "app.test"
    minSdk = 33
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
	
            
    create("debugConfig") {
      storeFile = file("${rootDir}/app/test.keystore.jks")
      storePassword = "android"
      keyAlias = "android"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  buildFeatures {
    compose = false
    buildConfig = true
  }

  packaging {
    resources {
      excludes.add("META-INF/*.kotlin_module")
      excludes.add("**/*.kotlin_module")
      excludes.add("**/*.kotlin_metadata")
      excludes.add("kotlin/**")
      excludes.add("org/jetbrains/annotations/**")
    }
  }



tasks.register("copyApkToRoot") {
  notCompatibleWithConfigurationCache("Copies APK to root directory")
  doLast {
    val apkFile = File(layout.buildDirectory.get().asFile, "outputs/apk/debug/app-debug.apk")
    if (apkFile.exists()) {
      val destFile = File(rootDir, "AiStudioBase.apk")
      apkFile.copyTo(destFile, overwrite = true)
      println("APK successfully copied to: ${destFile.absolutePath}")
    } else {
      println("APK file not found at: ${apkFile.absolutePath}")
    }
  }
}

tasks.matching { it.name == "assembleDebug" }.configureEach {
  finalizedBy("copyApkToRoot")
}


  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Exclude Kotlin libraries and JetBrains annotations transitively from all configurations
configurations.all {
  exclude(group = "org.jetbrains.kotlin")
  exclude(group = "org.jetbrains")
  exclude(group = "org.jetbrains", module = "annotations")
}

dependencies {
}

