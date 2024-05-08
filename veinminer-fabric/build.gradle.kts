plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.shadow)
}

dependencies {
    api(project(":veinminer-common"))
    shadow(project(":veinminer-common"))
    modApi(libs.choco.networking.fabric)

    minecraft(libs.minecraft.get())
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)

    modImplementation(libs.fabric.api)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

loom {
    accessWidenerPath = file("src/main/resources/veinminer_companion.accesswidener")
}

tasks {
    processResources {
        outputs.upToDateWhen { false } // Always process resource properties

        filesMatching("fabric.mod.json") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${project.name}" }
        }
    }

    shadowJar {
        configurations = listOf(project.configurations["shadow"], project.configurations["modApi"])
        exclude("META-INF")

        dependencies {
            include(project(":veinminer-common"))
            include(dependency(libs.choco.networking.common.get()))
            include(dependency(libs.choco.networking.fabric.get()))
        }
    }

    remapJar {
        dependsOn("shadowJar")
        mustRunAfter("shadowJar")
        inputFile.set(shadowJar.get().archiveFile)
    }
}
