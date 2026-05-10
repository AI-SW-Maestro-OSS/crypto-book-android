plugins {
    alias(libs.plugins.cryptobook.android.presentation)
    alias(libs.plugins.cryptobook.android.library.compose)
    alias(libs.plugins.cryptobook.spotless)
}

android {
    namespace = "io.soma.cryptobook.search.presentation"
}

dependencies {
    implementation(projects.core.presentation)
    implementation(projects.core.designsystem)
    implementation(libs.coil.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}