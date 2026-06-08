// Top-level build file — configuration here applies to all subprojects

// Redirect root build output outside OneDrive alongside the app module
layout.buildDirectory.set(File(System.getProperty("user.home"), "pani-build/root"))

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
}
