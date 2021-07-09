plugins {
    val kotlinVersion = "1.4.31"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.6.7"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "org.Reforward"
version = "1.0.0"

dependencies {
    val autoService = "1.0-rc7"
    kapt("com.google.auto.service", "auto-service", autoService)
    compileOnly("com.google.auto.service", "auto-service-annotations", autoService)
    runtimeOnly("org.slf4j","slf4j-simple","1.7.30")
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven { url = uri("https://dl.bintray.com/karlatemp/misc") }
}