plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}


android {
    namespace = "com.omdbmovies.movieverse"
    compileSdk = 35

    val apiKey: String = project.findProperty("API_KEY") as? String
        ?: throw GradleException("API_KEY not found in gradle.properties")
    defaultConfig {
        applicationId = "com.omdbmovies.movieverse"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

// Mockito for mocking dependencies
    testImplementation ("org.mockito:mockito-core:4.0.0")
    testImplementation ("org.mockito:mockito-inline:4.0.0")
    testImplementation ("io.mockk:mockk:1.13.8")

// Retrofit and MockWebServer
    testImplementation ("com.squareup.retrofit2:retrofit:2.9.0")
    testImplementation ("com.squareup.okhttp3:mockwebserver:4.9.0")

// Coroutine testing dependencies (if using coroutines in the network layer)
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
    testImplementation ("androidx.arch.core:core-testing:2.2.0")



    implementation("com.facebook.shimmer:shimmer:0.5.0")
    // Hilt
    implementation ("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")


// Retrofit & Gson
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

// ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

// Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// RecyclerView
    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt ("com.github.bumptech.glide:compiler:4.16.0")

    kapt("androidx.room:room-compiler:2.6.0")

    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")

//    implementation ("androidx.room:room-ktx:2.2.1")
//    kapt ("androidx.room:room-compiler:2.2.1")

    // Jetpack Navigation component
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")

// Material Components (for BottomNavigationView)
    implementation("com.google.android.material:material:1.11.0")

}

tasks.withType<Test> {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
}