pluginManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

rootProject.name = "VeinMiner"

include("veinminer-common", "veinminer-server", "veinminer-bukkit", "veinminer-fabric")
