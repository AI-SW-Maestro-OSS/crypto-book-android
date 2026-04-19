import java.util.Properties

plugins {
    alias(libs.plugins.cryptobook.android.library)
    alias(libs.plugins.cryptobook.hilt)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.cryptobook.spotless)
}

android {
    namespace = "io.soma.cryptobook.core.data"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        buildConfigField(
            "String",
            "EXCHANGE_API_KEY",
            "\"${properties.getProperty("exchange.api.key", "")}\""
        )
    }
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.network)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.androidx.dataStore)
    implementation(libs.protobuf.kotlin.lite)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
