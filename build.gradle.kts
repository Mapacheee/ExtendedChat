plugins {
    java
    `java-library`
    id("com.gradleup.shadow") version "9.2.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "me.mapacheee"
version = "1.0.0-SNAPSHOT"
description = "ExtendedChat - Advanced chat plugin for modern Minecraft"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnlyApi("me.mapacheee:MapacheeeLib:1.0.0")
    annotationProcessor("me.mapacheee:MapacheeeLib:1.0.0")
    annotationProcessor("com.thewinterframework:paper:1.0.6")
    annotationProcessor("com.thewinterframework:command:1.0.1")
    annotationProcessor("com.thewinterframework:configuration:1.0.4")

    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.12.2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks {
    processResources {
        filesMatching("paper-plugin.yml") {
            expand(mapOf("version" to project.version, "description" to project.description))
        }
    }
    shadowJar {
        archiveFileName.set("ExtendedChat-${project.version}.jar")
        archiveClassifier.set("")
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
        mergeServiceFiles()
    }
    build {
        dependsOn(shadowJar)
    }
}