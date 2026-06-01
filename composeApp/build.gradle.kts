import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.0.0"
    id("org.jetbrains.kotlinx.kover")

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
            implementation("androidx.activity:activity-compose:1.8.2")
            implementation("cafe.adriel.voyager:voyager-hilt:${voyagerVersion}")
            implementation("cafe.adriel.voyager:voyager-livedata:${voyagerVersion}")
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)

            implementation("io.insert-koin:koin-android:3.5.0")
            implementation("io.insert-koin:koin-androidx-compose:3.5.0")
        }
        commonMain.dependencies {

            implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha06")
            implementation("io.coil-kt.coil3:coil-network-ktor:3.0.0-alpha06")

            implementation(libs.compose.webview.multiplatform)
            implementation("net.openid:appauth:0.11.1")
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.6.11")
            val voyagerVersion = "1.1.0-beta02"
            implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-koin:$voyagerVersion")

            implementation("network.chaintech:kmp-date-time-picker:1.0.6")
            implementation("cafe.adriel.voyager:voyager-hilt:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-livedata:$voyagerVersion")
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlin.testJunit)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            implementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(libs.compose.uiToolingPreview)
        }
    }
}

android {
    namespace = "com.example.track_me_mobile"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "com.example.track_me_mobile"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["appAuthRedirectScheme"] = "com.example.trackme"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
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
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.0")
    debugImplementation(libs.compose.uiTooling)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.8")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

kover {
    reports {
        filters {
            includes {
                classes("com.example.track_me_mobile.features.*")
            }
            excludes {
                classes(
                    "*.ComposableSingletons*",
                    "*MeetingComponentsKt",
                    "*MeetingScreen*",
                    "*ReportsListScreen*",
                    "*StreamMeetingReportScreen*",
                    "*LoginScreen*",
                    "*LoginWebViewScreen*",
                    "com.example.track_me_mobile.features.*.presentation.*Kt",
                    "com.example.track_me_mobile.features.*.presentation.*Screen",
                    "com.example.track_me_mobile.features.*.presentation.*ScreenKt",
                    "com.example.track_me_mobile.features.*.presentation.*ComponentsKt",
                    "com.example.track_me_mobile.features.*.presentation.components.*Kt",
                    "com.example.track_me_mobile.features.streams.presentation.components.*Kt",
                    "com.example.track_me_mobile.features.tracker_list.presentation.components.*Kt",
                    "com.example.track_me_mobile.features.*.presentation.ImagePicker*",
                    "com.example.track_me_mobile.features.*.presentation.ImageLoader*",
                    "com.example.track_me_mobile.features.auth.data.*",
                    "com.example.track_me_mobile.features.meetings.data.*",
                    "com.example.track_me_mobile.features.streams.data.*",
                    "com.example.track_me_mobile.features.team_card.data.*",
                    "com.example.track_me_mobile.features.streams.presentation.components",
                    "com.example.track_me_mobile.features.reports.presentation.components",
                    "*AuthRepositoryImpl",
                    "*MeetingRepositoryImpl",
                    "*StreamRepositoryImpl",
                    "*TeamCardRepositoryImpl",
                    "com.example.track_me_mobile.features.auth.data.AuthRepositoryImpl",
                    "com.example.track_me_mobile.features.meetings.data.MeetingRepositoryImpl",
                    "com.example.track_me_mobile.features.team_card.data.TeamCardRepositoryImpl"
                )
            }
        }
    }
}

compose.resources {
    publicResClass =  true
    packageOfResClass = "com.example.track_me_mobile.generated.resources"
    generateResClass = auto
}

