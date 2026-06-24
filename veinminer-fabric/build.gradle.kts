plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.shadow)
}

dependencies {
    api(project(":veinminer-common"))
    shadow(project(":veinminer-common"))
    api(libs.choco.networking.fabric)
    shadow(libs.choco.networking.fabric)

    minecraft(libs.minecraft.get())
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    withType<JavaCompile> {
        options.release = 25
    }

    processResources {
        outputs.upToDateWhen { false } // Always process resource properties

        filesMatching("fabric.mod.json") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        dependsOn("shadowJar")
        mustRunAfter("shadowJar")

        from("LICENSE") {
            rename { "${it}_${project.name}" }
        }
    }

    shadowJar {
        configurations.addAll(project.configurations["shadow"])
        exclude("META-INF")

        dependencies {
            include(project(":veinminer-common"))
            include(dependency(libs.choco.networking.common.get()))
            include(dependency(libs.choco.networking.fabric.get()))
        }
    }
}
