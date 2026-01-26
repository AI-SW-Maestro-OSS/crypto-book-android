plugins {
    alias(libs.plugins.cryptobook.android.presentation)
    alias(libs.plugins.cryptobook.android.library.compose)
    alias(libs.plugins.cryptobook.spotless)
}

android {
    namespace = "io.soma.cryptobook.main.presentation"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.splash.presentation)
    implementation(projects.coinDetail.presentation)
    implementation(projects.home.presentation)
    implementation(projects.settings.presentation)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(libs.kotlinx.atomicfu)

    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigation3)
    implementation(libs.androidx.compose.material3.navigationSuite)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.core.splashscreen)

    // Coil (Image Loading)
    implementation(libs.coil.compose)
}
