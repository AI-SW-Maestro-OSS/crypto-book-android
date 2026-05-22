plugins {
    alias(libs.plugins.cryptobook.android.library.compose)
    alias(libs.plugins.cryptobook.android.presentation)
    alias(libs.plugins.cryptobook.spotless)
}

android {
    namespace = "io.soma.cryptobook.core.presentation"
}

dependencies {
    api(libs.androidx.metrics)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
