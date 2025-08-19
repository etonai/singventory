plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.pseddev.singventory"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pseddev.singventory"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Optimize for Google Play Store
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // Enable resource shrinking for all variants
        androidResources {
            localeFilters += listOf("en")
        }
        
        // Optimize dex compilation
        multiDexEnabled = false
    }

    signingConfigs {
        create("release") {
            storeFile = file("../keystore/singventory-release-secure.keystore")
            storePassword = "Sv7Mp9Kx2Qw8Zt3Nc6Vb4"
            keyAlias = "singventory"
            keyPassword = "Sv7Mp9Kx2Qw8Zt3Nc6Vb4"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // APK naming for release builds
            buildConfigField("boolean", "IS_DEBUG_BUILD", "false")
            
            // Optimize for release
            manifestPlaceholders["crashlyticsCollectionEnabled"] = true
        }
        
        debug {
            isDebuggable = true
            buildConfigField("boolean", "IS_DEBUG_BUILD", "true")
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
    
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
    
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val appName = "Singventory"
            val versionName = variant.versionName
            val buildType = variant.buildType.name
            
            // Different naming for release vs debug builds
            output.outputFileName = if (buildType == "release") {
                "${appName}-v${versionName}-${buildType}.apk"
            } else {
                "${appName}-v${versionName}-${buildType}.apk"
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    
    // Room database dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Navigation and Fragment dependencies
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.fragment.ktx)
    
    // Testing dependencies (debug and test builds only)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    
    // Debug-only dependencies (removed from release builds)
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.7")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.7")
}