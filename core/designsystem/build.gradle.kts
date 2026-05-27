plugins {
    alias(libs.plugins.cryptobook.android.library)
    alias(libs.plugins.cryptobook.android.library.compose)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "io.soma.cryptobook.core.designsystem"
}

dependencies {
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigation3)
    implementation(libs.androidx.compose.material3.navigationSuite)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.coil.compose)
}