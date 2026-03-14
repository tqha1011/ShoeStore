// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

sonar {
    properties {
        property("sonar.projectKey", "tqha1011_ShoeStore_Frontend")
        property("sonar.organization", "tqha1011")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}



