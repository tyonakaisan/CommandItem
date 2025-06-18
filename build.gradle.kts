plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")

    // PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Paper
    compileOnly("io.papermc.paper", "paper-api", "1.20.6-R0.1-SNAPSHOT")

    // Config
    paperLibrary("org.spongepowered", "configurate-hocon", "4.2.0")
    paperLibrary("net.kyori", "adventure-serializer-configurate4", "4.21.0")

    // Plugins
    compileOnly("me.clip", "placeholderapi", "2.11.5")
    compileOnly("io.github.miniplaceholders", "miniplaceholders-api", "2.2.3")

    // Utils
    paperLibrary("com.google.inject", "guice", "7.0.0")
    paperLibrary("net.objecthunter", "exp4j", "0.4.8")
}

version = "1.7.0-SNAPSHOT"

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

    serverDependencies {
        register("PlaceholderAPI") {
            required = false
        }

        register("MiniPlaceholders") {
            required = false
        }
    }
}

tasks {
    val paperPlugins = runPaper.downloadPluginsSpec {
        // PlaceholderAPI
        // hangar("PlaceholderAPI", "2.11.5")
        // MiniPlaceholders
        github("MiniPlaceholders", "MiniPlaceholders", "2.3.0", "MiniPlaceholders-Paper-2.3.0.jar")
        // github("MiniPlaceholders", "PlaceholderAPI-Expansion", "1.2.0", "PlaceholderAPI-Expansion-1.2.0.jar")
        github("MiniPlaceholders", "Player-Expansion", "1.2.0", "MiniPlaceholders-Player-Expansion-1.2.0.jar")
        github("MiniPlaceholders", "Expressions-Expansion", "1.2.0", "Expressions-Expansion-1.2.0.jar")
    }

    compileJava {
        this.options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    shadowJar {
        this.archiveClassifier.set(null as String?)
        archiveVersion.set(paper.version)
    }

    runServer {
        minecraftVersion("1.21.1")

        downloadPlugins {
            downloadPlugins.from(paperPlugins)
        }
    }

    test {
        useJUnitPlatform()
    }
}