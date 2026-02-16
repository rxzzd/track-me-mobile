import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            val voyagerVersion = "1.1.0-beta02"
            // Android
// Hilt integration
            implementation("cafe.adriel.voyager:voyager-hilt:${voyagerVersion}")
// LiveData integration
            implementation("cafe.adriel.voyager:voyager-livedata:${voyagerVersion}")
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {

<<<<<<< Updated upstream
=======
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.compose.webview.multiplatform)


>>>>>>> Stashed changes
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            val voyagerVersion = "1.1.0-beta02"
            // Multiplatform
            // Navigator
            implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
            // Screen Model
            implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
            // BottomSheetNavigator
            implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
            // TabNavigator
            implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
            // Transitions
            implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
            // Koin integration
            implementation("cafe.adriel.voyager:voyager-koin:$voyagerVersion")

            //implementation(compose.materialIconsExtended)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.track_me_mobile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.track_me_mobile"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.resources {
    publicResClass =  true
    packageOfResClass = "com.example.track_me_mobile.generated.resources"
    generateResClass = auto
}

