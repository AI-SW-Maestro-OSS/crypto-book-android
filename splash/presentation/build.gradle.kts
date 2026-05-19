plugins {
    alias(libs.plugins.cryptobook.android.presentation)
    alias(libs.plugins.cryptobook.android.library.compose)
    alias(libs.plugins.cryptobook.spotless)
}

android {
    namespace = "io.soma.cryptobook.splash.presentation"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.splash.domain)
}