plugins {
    alias(libs.plugins.cryptobook.jvm.library)
    alias(libs.plugins.cryptobook.hilt)
    alias(libs.plugins.cryptobook.spotless)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
