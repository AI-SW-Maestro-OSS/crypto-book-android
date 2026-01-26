plugins {
    alias(libs.plugins.cryptobook.android.presentation)
    alias(libs.plugins.cryptobook.android.library.compose)
    alias(libs.plugins.cryptobook.spotless)
}

android {
    namespace = "io.soma.cryptobook.home.presentation"
}

dependencies {
    implementation(projects.core.presentation)
    implementation(projects.core.designsystem)
    implementation(projects.home.domain)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
