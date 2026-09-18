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
        versionCode = 10
        versionName = "0.0.10-prototype"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
