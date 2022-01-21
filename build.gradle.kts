plugins {
    kotlin("jvm") version "1.6.10"
}

group = "dev.reeve"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-api:0.3.0")
	implementation("org.jetbrains.kotlinx:kotlin-deeplearning-visualization:0.3.0")
	implementation("org.tensorflow:libtensorflow:1.15.0")
	implementation("org.tensorflow:libtensorflow_jni_gpu:1.15.0")
	
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")
	implementation("org.slf4j:slf4j-log4j12:1.7.32")
}