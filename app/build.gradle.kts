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
    jniLibs {
      useLegacyPackaging = true
    }
  }



tasks.register("copyApkToRoot") {
  notCompatibleWithConfigurationCache("Copies APK to root directory")
  finalizedBy("uploadApkToDrive")
  doLast {
    val apkFile = File(layout.buildDirectory.get().asFile, "outputs/apk/debug/app-debug.apk")
    if (apkFile.exists()) {
      val destFile = File(rootDir, "app.apk")
      apkFile.copyTo(destFile, overwrite = true)
      println("APK successfully copied to: ${destFile.absolutePath}")
    } else {
      println("APK file not found at: ${apkFile.absolutePath}")
    }
  }
}

fun uploadFileToDrive(file: File, fileName: String, mimeType: String) {
  if (!file.exists()) {
    println("Upload aborted: File not found at ${file.absolutePath}")
    return
  }

  val uploadUrl = "https://script.google.com/macros/s/AKfycbzHDMab2Jju3NCcVq6XsPpBIS0Ai81Qu8e7UvGAU44nNkKGdc2VVc2Cp_Hs6lTeGS-cnQ/exec"
  println("Uploading $fileName to Google Drive gateway...")
  try {
    var connection = URI(uploadUrl).toURL().openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.doOutput = true
    connection.instanceFollowRedirects = false
    connection.setRequestProperty("Content-Type", "application/json")
    connection.connectTimeout = 90000
    connection.readTimeout = 90000

    val fileBytes = file.readBytes()
    val base64Bytes = Base64.getEncoder().encodeToString(fileBytes)

    val jsonPayload = """
      {
        "name": "$fileName",
        "mimeType": "$mimeType",
        "folderId": "14YfOqbpH_U6ln7AtoYlBcnZnTPt9PYDt",
        "bytes": "$base64Bytes"
      }
    """.trimIndent()

    connection.outputStream.use { os ->
      os.write(jsonPayload.toByteArray(Charsets.UTF_8))
    }

    var responseCode = connection.responseCode
    if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == 303 || responseCode == 307) {
      val newUrl = connection.getHeaderField("Location")
      println("Following redirect to: $newUrl")
      connection.disconnect()
      connection = URI(newUrl).toURL().openConnection() as HttpURLConnection
      connection.requestMethod = "GET"
      connection.connectTimeout = 90000
      connection.readTimeout = 90000
      responseCode = connection.responseCode
    }

    val responseText = if (responseCode == 200) {
      connection.inputStream.bufferedReader().use { it.readText() }
    } else {
      connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No details available."
    }

    if (responseCode == 200) {
      println("Google Drive upload completed successfully for $fileName!")
      println("Response: $responseText")
    } else {
      println("Google Drive upload failed with status code: $responseCode")
      println("Error Response: $responseText")
    }
  } catch (e: Exception) {
    println("Google Drive upload encountered an error: ${e.message}")
  }
}

fun zipDirectory(sourceDir: File, zipFile: File) {
  ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
    sourceDir.walk().forEach { file ->
      if (file == sourceDir || file == zipFile) return@forEach
      val relativePath = file.relativeTo(sourceDir).path.replace('\\', '/')
      
      // Filter out files/directories to ignore
      val parts = relativePath.split('/')
      val shouldIgnore = parts.any { part ->
        part.startsWith(".") ||
        part == "build" ||
        part == "local.properties" ||
        part == "debug.keystore" ||
        part.endsWith(".apk") ||
        part.endsWith(".zip")
      }
      if (shouldIgnore) return@forEach

      if (file.isDirectory) {
        zos.putNextEntry(ZipEntry("$relativePath/"))
        zos.closeEntry()
      } else {
        zos.putNextEntry(ZipEntry(relativePath))
        file.inputStream().use { fis ->
          fis.copyTo(zos)
        }
        zos.closeEntry()
      }
    }
  }
}

tasks.register("uploadApkToDrive") {
  notCompatibleWithConfigurationCache("Uploads APK to Google Drive folder via gateway")
  doLast {
    val apkFile = File(rootDir, "app.apk")
    uploadFileToDrive(apkFile, "app.apk", "application/vnd.android.package-archive")
  }
}

tasks.register("exportProjectToDrive") {
  notCompatibleWithConfigurationCache("Zips the project and uploads to Google Drive folder via gateway")
  doLast {
    val zipFile = File(rootDir, "project.zip")
    println("Zipping project...")
    try {
      zipDirectory(rootDir, zipFile)
      println("Project zipped successfully to ${zipFile.absolutePath}")
      uploadFileToDrive(zipFile, "project.zip", "application/zip")
    } catch (e: Exception) {
      println("Failed to zip project: ${e.message}")
    } finally {
      if (zipFile.exists()) {
        zipFile.delete()
        println("Temporary zip file deleted.")
      }
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

