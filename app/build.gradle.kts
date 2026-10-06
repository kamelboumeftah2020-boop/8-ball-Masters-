import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

/** Optional settings from gradle properties, local.properties or the environment. */
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun setting(name: String): String =
    (project.findProperty(name) as String?) ?: localProps.getProperty(name) ?: System.getenv(name) ?: ""

android {
    namespace = "com.fluently.english"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fluently.english"
        minSdk = 24
        targetSdk = 35
        versionCode = 16
        versionName = "1.10.0"
        // Cloud accounts: set FIREBASE_API_KEY and FIREBASE_PROJECT_ID (see README).
        // Without them, accounts and progress are kept on the device.
        // Phones only (the offline speech engine ships native code per CPU type).
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
        buildConfigField("String", "FIREBASE_API_KEY", "\"${setting("FIREBASE_API_KEY")}\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${setting("FIREBASE_PROJECT_ID")}\"")
    }

    signingConfigs {
        // The permanent release key (kept outside the repository): RELEASE_STORE_FILE,
        // RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS and RELEASE_KEY_PASSWORD in
        // local.properties or the environment.
        val storeFile = setting("RELEASE_STORE_FILE")
        if (storeFile.isNotBlank() && rootProject.file(storeFile).exists()) {
            create("release") {
                this.storeFile = rootProject.file(storeFile)
                storePassword = setting("RELEASE_STORE_PASSWORD")
                keyAlias = setting("RELEASE_KEY_ALIAS")
                keyPassword = setting("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // The permanent release key when available; otherwise the debug key so the
            // build still produces an installable APK (e.g. on CI without the secrets).
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    // Robolectric runs the microphone tests on the JVM.
    testOptions { unitTests.isIncludeAndroidResources = true }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.core:core-splashscreen:1.0.1")
    // Offline English speech recognition for phones without Google's speech service.
    implementation("com.alphacephei:vosk-android:0.3.75")
    implementation("net.java.dev.jna:jna:5.18.1@aar")
    // Unity Ads (interstitial, rewarded and banner ads).
    implementation("com.unity3d.ads:unity-ads:4.21.0")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation("org.json:json:20240303")
}
