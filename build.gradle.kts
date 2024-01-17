plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.2.0"
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

    // PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper", "paper-api", "1.20.2-R0.1-SNAPSHOT")

    // Command
    paperLibrary("cloud.commandframework", "cloud-paper", "1.8.3")

    // Config
    implementation("org.spongepowered", "configurate-hocon", "4.1.2")
    implementation("net.kyori", "adventure-serializer-configurate4", "4.14.0")

    // Plugins
    compileOnly("me.clip", "placeholderapi", "2.11.5")
    compileOnly("io.github.miniplaceholders", "miniplaceholders-api", "2.2.3")

    // Utils
    paperLibrary("com.google.inject", "guice", "7.0.0")
    paperLibrary("co.aikar", "taskchain-bukkit", "3.7.2")
    paperLibrary("net.objecthunter", "exp4j", "0.4.8")
}

version = "1.3.1-SNAPSHOT"

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
        // TabTps
        url("https://cdn.modrinth.com/data/cUhi3iB2/versions/QmxLremu/tabtps-spigot-1.3.21.jar")
        // Spark
        url("https://ci.lucko.me/job/spark/396/artifact/spark-bukkit/build/libs/spark-1.10.55-bukkit.jar")
        // PlaceholderAPI
        hangar("PlaceholderAPI", "2.11.5")
        // MiniPlaceholders
        github("MiniPlaceholders", "MiniPlaceholders", "2.2.3", "MiniPlaceholders-Paper-2.2.3.jar")
        github("MiniPlaceholders", "PlaceholderAPI-Expansion", "1.2.0", "PlaceholderAPI-Expansion-1.2.0.jar")
        github("MiniPlaceholders", "Player-Expansion", "1.2.0", "MiniPlaceholders-Player-Expansion-1.2.0.jar")
        github("MiniPlaceholders", "Expressions-Expansion", "1.2.0", "Expressions-Expansion-1.2.0.jar")
    }

    compileJava {
        this.options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    shadowJar {
        this.archiveClassifier.set(null as String?)
        relocate("org.spongepowered.configurate", "github.tyonakaisan.commanditem.configurate")
    }

    runServer {
        minecraftVersion("1.20.2")

        downloadPlugins {
            downloadPlugins.from(paperPlugins)
        }
    }
}