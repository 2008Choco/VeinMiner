plugins {
    alias(libs.plugins.fabric.loom)
}

dependencies {
    implementation(project(":veinminer-common"))

    minecraft(libs.minecraft.get())
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)

    modImplementation(libs.fabric.api)
    modImplementation(libs.choco.networking.fabric)

    include(project(":veinminer-common"))
    include(libs.choco.networking.common)
    include(libs.choco.networking.fabric)
}

loom {
    accessWidenerPath = file("src/main/resources/veinminer_companion.accesswidener")
}

tasks {
    processResources {
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
}
