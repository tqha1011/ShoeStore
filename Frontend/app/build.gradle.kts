import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Load properties from local.properties file
val properties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(localPropertiesFile.inputStream())
}

// Fetch sensitive data from local.properties
val googleClientId = properties.getProperty("GOOGLE_CLIENT_ID") ?: ""
val baseUrl = properties.getProperty("BASE_URL") ?: ""

android {
    namespace = "com.example.shoestoreapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.shoestoreapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject sensitive constants into BuildConfig for app-wide access
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        buildConfig = true // Ensure BuildConfig is enabled
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // Retrofit & Gson
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.androidx.navigation.compose)
    // OkHttp Logging
    implementation(libs.okhttp.logging)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    // DataStore
    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle.runtime.ktx)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.foundation) // Icons Library
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
}