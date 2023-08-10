plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")

    // Paper
    maven("https://repo.papermc.io/repository/maven-public/")

    // TaskChain
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper", "paper-api", "1.20.1-R0.1-SNAPSHOT")

    // Command
    paperLibrary("cloud.commandframework", "cloud-paper", "1.8.3")

    // Config
    paperLibrary("org.spongepowered", "configurate-hocon", "4.1.2")
    paperLibrary("net.kyori", "adventure-serializer-configurate4", "4.12.0")

    // Messages
    paperLibrary("net.kyori.moonshine", "moonshine-standard", "2.0.4")

    // Utils
    paperLibrary("com.google.inject", "guice", "7.0.0")
    paperLibrary("co.aikar", "taskchain-bukkit", "3.7.2")
}

version = "1.0-SNAPSHOT"

paper {
    val mainPackage = "github.tyonakaisan.commanditem"
    generateLibrariesJson = true
    name = rootProject.name
    version = project.version as String
    main = "$mainPackage.CommandItem"
    loader = "$mainPackage.CommandItemLoader"
    bootstrapper = "$mainPackage.CommandItemBootstrap"
    apiVersion = "1.20"
    author = "tyonakaisan"
    website = "https://github.com/tyonakaisan"
}

tasks {
    compileJava {
        this.options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    shadowJar {
        this.archiveClassifier.set(null as String?)
    }

    runServer {
        minecraftVersion("1.20.1")
    }
}