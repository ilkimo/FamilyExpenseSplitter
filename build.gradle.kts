
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.application"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // For plotting with XChart
    implementation("org.knowm.xchart:xchart:3.8.1")
}

application {
    mainClass.set("com.application.AppKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

// No need to configure the 'jar' task manually
