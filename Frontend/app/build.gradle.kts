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
val fbAppId = properties.getProperty("FACEBOOK_APP_ID") ?: ""
val fbClientToken = properties.getProperty("FACEBOOK_CLIENT_TOKEN") ?: ""
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

        // Facebook
        resValue("string", "facebook_app_id", fbAppId)
        resValue("string", "facebook_client_token", fbClientToken)
        resValue("string", "fb_login_protocol_scheme", "fb${fbAppId}")
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
    implementation(libs.coil.compose)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation(libs.foundation) // Icons Library
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    implementation(libs.facebook.login)
    implementation(libs.androidx.foundation.layout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

}
