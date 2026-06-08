plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(libs.android.gradleApiPlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = libs.plugins.cryptobook.android.library.asProvider().get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("hilt") {
            id = libs.plugins.cryptobook.hilt.get().pluginId
            implementationClass = "HiltConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = libs.plugins.cryptobook.android.library.compose.get().pluginId
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidPresentation") {
            id = libs.plugins.cryptobook.android.presentation.get().pluginId
            implementationClass = "AndroidPresentationConventionPlugin"
        }
        register("jvmLibrary") {
            id = libs.plugins.cryptobook.jvm.library.get().pluginId
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("spotless") {
            id = libs.plugins.cryptobook.spotless.get().pluginId
            implementationClass = "SpotlessConventionPlugin"
        }
    }
}