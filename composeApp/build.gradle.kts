import com.android.build.gradle.tasks.factory.AndroidUnitTest
import live.ditto.gradle.EnvGradleTask
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    compilerOptions {
        apiVersion.set(KOTLIN_2_0)
    }
    metadata {
        compilations.configureEach {
            // Custom task which generates the Env object. Needs to be run before compileCommonMainKotlinMetadata
            compileTaskProvider.get().dependsOn("envTask")
        }
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant {
            sourceSetTree.set(KotlinSourceSetTree.test)

            dependencies {
                implementation(libs.androidx.compose.ui.test.junit4.android)
                debugImplementation(libs.androidx.compose.ui.test.manifest)
            }
        }
    }

    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        framework {
            baseName = "commonMain"
            isStatic = false
        }
        summary = "Sync apps with or without the internet"
        homepage = "https://ditto.live/"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")

        pod("DittoObjC") {
            version = libs.versions.ditto.get()
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.koin.core)
            implementation(compose.components.resources)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.accompanist.permissions)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.compose.foundation.android)
            implementation(libs.androidx.compose.runtime.android)
            implementation(libs.androidx.compose.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.ditto)
            implementation(libs.koin.android)
        }
    }
}


composeCompiler {
    enableStrongSkippingMode = true
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

compose.resources {
    publicResClass = true
    packageOfResClass = ""
    generateResClass = always
}

android {
    namespace = "live.ditto.demo.kotlinmultipeer"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    android.buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "live.ditto.demo.kotlinmultipeer"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    dependencies {
        testImplementation(libs.kotlin.test)
        testImplementation(libs.testing.junit)
        androidTestImplementation(libs.kotlin.test)
        androidTestImplementation(libs.testing.junit)
    }
}

tasks {
    // Dummy testClasses task to resolve error:
    // > Cannot locate tasks that match ':shared:testClasses' as task 'testClasses' not found in project ':shared'.
    val testClasses by registering

    // Android app environment variables
    val envFile = rootProject.file("env.properties")
    if (!envFile.exists()) {
        throw Exception(
            "Missing env.properties file. Please copy the env.properties.example template and fill in with your app details.",
        )
    }
    val env = Properties()
    env.load(FileInputStream(envFile))

    val envTask by registering(EnvGradleTask::class) {
        className = "Env"
        packageName = ""
        sourceDir = file("src/commonMain/kotlin")
        DEBUG = true
        VERSION = project.version as String
        DITTO_APP_ID = env["DITTO_APP_ID"] as String
        DITTO_OFFLINE_TOKEN = env["DITTO_OFFLINE_TOKEN"] as String
        DITTO_PLAYGROUND_TOKEN = env["DITTO_PLAYGROUND_TOKEN"] as String
    }

    val podClean by registering(Delete::class) {
        description = "Clean the Podfile.lock file"
        delete += listOf("$rootDir/iosApp/Podfile.lock")
    }
//    val podInstall by getting {
//        dependsOn(podClean)
//    }

    // compileDebugKotlinAndroid
    withType<KotlinJvmCompile>()
        .configureEach {
            compilerOptions
                .jvmTarget
                .set(JVM_11)
        }
    withType<KotlinCompile> {
        // Ensure the [Env] object has been generated
        dependsOn(envTask)
    }
    // compileKotlinIosSimulatorArm64
    withType<KotlinNativeCompile> {
        dependsOn(envTask)
    }

    clean {
        // Remove generated Env.kt file
        delete += listOf("$rootDir/composeApp/src/commonMain/kotlin/Env.kt")
    }
}
