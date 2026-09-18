plugins {
    id("com.android.application")
}

android {
    namespace = "io.github.joelmomo.runeboard"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.joelmomo.runeboard"
        minSdk = 33
        targetSdk = 36
        versionCode = 2
        versionName = "0.0.2-prototype"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
