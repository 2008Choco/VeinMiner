plugins {
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot

    maven("https://nexus.hc.to/content/repositories/pub_releases/") // Vault API
    maven("https://maven.enginehub.org/repo/") // WorldGuard
    maven("https://nexus.neetgames.com/repository/maven-public/") // mcMMO

    maven("https://jitpack.io") // mcMMO, Grim AntiCheat, Matrix AntiCheat, Light AntiCheat
    maven("https://repo.janmm14.de/repository/public/") // Advanced AntiCheat
    maven("https://repo.md-5.net/content/repositories/snapshots/") // NoCheatPlus
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
}

dependencies {
    compileOnly(libs.spigot.api)

    implementation(project(":veinminer-common"))

    implementation(libs.bstats.bukkit)
    implementation(libs.choco.networking.bukkit) {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }

    compileOnly(libs.placeholder.api)
    compileOnly(libs.vault.api)
    compileOnly(libs.worldguard)
    compileOnly(libs.mcmmo)

    // Anti-cheats
    compileOnly(libs.anticheat.aac)
    compileOnly(libs.anticheat.grim)
    compileOnly(libs.anticheat.matrix)
    compileOnly(libs.anticheat.nocheatplus)
    compileOnly(libs.anticheat.spartan)
    compileOnly(libs.anticheat.light)
    compileOnly(libs.anticheat.negativity)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks {
    processResources {
        outputs.upToDateWhen { false } // Always process resource properties

        filesMatching("plugin.yml") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }

    build {
        dependsOn("shadowJar")
    }

    withType<Javadoc>() {
        exclude("wtf/choco/veinminer/listener/**")
        exclude("wtf/choco/veinminer/command/**")
    }

    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        relocate("org.bstats", "wtf.choco.veinminer.metrics")
        archiveFileName = "VeinMiner-Bukkit-${version}.jar"

        dependencies {
            include(project(":veinminer-common"))
            include(dependency(libs.bstats.base.get()))
            include(dependency(libs.bstats.bukkit.get()))
            include(dependency(libs.choco.networking.common.get()))
            include(dependency(libs.choco.networking.bukkit.get()))
        }
    }
}

// Strip out shadowed artifacts from publications
val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
    skip()
}
