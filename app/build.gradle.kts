plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}


android {
    compileSdkVersion(29)
    buildToolsVersion("28.0.3")
    defaultConfig {
        applicationId("org.openvoipalliance.phonelibexample")
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        addDefaultAuthValues(this)
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    lintOptions {
        isAbortOnError = false
        isCheckReleaseBuilds = false
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72")
    implementation(project(":AndroidPhoneLib"))

    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0") {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    testImplementation("junit:junit:4.13.1")
}

fun addDefaultAuthValues(defaultConfig: com.android.build.api.dsl.DefaultConfig) {
    // If you wish to pre-populate the example app with authentication information to
    // make testing quicker, just add these properties (e.g. apl.default.username) to
    // your ~/.gradle/gradle.properties file.
    try {
        defaultConfig.resValue("string", "default_sip_user", project.property("apl.default.username") as String)
        defaultConfig.resValue("string", "default_sip_password", project.property("apl.default.password") as String)
        defaultConfig.resValue("string", "default_sip_server", project.property("apl.default.server") as String)
        defaultConfig.resValue("string", "default_sip_port", project.property("apl.default.port") as String)
    } catch (e: groovy.lang.MissingPropertyException) {
        defaultConfig.resValue("string", "default_sip_user", "")
        defaultConfig.resValue("string", "default_sip_password", "")
        defaultConfig.resValue("string", "default_sip_server", "")
        defaultConfig.resValue("string", "default_sip_port", "")
    }
}