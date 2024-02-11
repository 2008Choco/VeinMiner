pluginManagement {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

rootProject.name = "VeinMiner"

include("veinminer-common", "veinminer-bukkit", "veinminer-fabric")
