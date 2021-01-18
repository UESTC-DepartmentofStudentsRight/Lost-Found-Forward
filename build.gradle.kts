plugins {
    val kotlinVersion = "1.4.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "org.Reforward"
version = "0.15.3"

dependencies {
    val autoService = "1.0-rc7"
    kapt("com.google.auto.service", "auto-service", autoService)
    compileOnly("com.google.auto.service", "auto-service-annotations", autoService)
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}