plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    id("com.kezong.fat-aar")
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${rootProject.extra["kotlinVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0")
    testImplementation("junit:junit:4.12")

    api("org.koin:koin-android:2.2.0-rc-4")
    embed("org.linphone:linphone-sdk-android:4.4+")
}

//tasks.withType(Javadoc).all {
//    enabled = false
//}


//ext {
//    bintrayRepo = 'AndroidPhoneLib'
//    bintrayName = 'AndroidPhoneLib'
//    publishedGroupId = 'org.openvoipalliance'
//    libraryName = 'AndroidPhoneLib'
//    artifact = 'AndroidPhoneLib'
//    libraryDescription = 'An Android library to faciliate SIP communication.'
//    siteUrl = 'https://github.com/open-voip-alliance/AndroidPhoneLib'
//    gitUrl = 'https://github.com/open-voip-alliance/AndroidPhoneLib.git'
//    libraryVersion = '0.5.28'
//    developerId = 'jeremy.norman'
//    developerName = 'Jeremy Norman'
//    developerEmail = 'jeremy.norman@wearespindle.com'
//    licenseName = 'The Apache Software License, Version 2.0'
//    licenseUrl = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
//    allLicenses = ['Apache-2.0']
//}
//
//apply from: 'https://raw.githubusercontent.com/nuuneoi/JCenter/master/installv1.gradle'
//apply from: 'https://raw.githubusercontent.com/nuuneoi/JCenter/master/bintrayv1.gradle'
