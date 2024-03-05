plugins {
    id("eclipse")
    id("java")
    id("maven-publish")
    // alias(libs.plugins.aggregate.javadoc) // Stupid idiot dumb plugin checks if "java" plugin is used on root, but can't use compileOnly() or implementation() UNLESS the "java" plugin is applied. So I guess fuck me then
}

/* Meant for Aggregate Javadocs but lol the plugin is stupid
dependencies {
    rootProject.subprojects.forEach { subproject ->
        subproject.plugins.withId("java") {
            javadoc(subproject)
        }
    }
}
*/

subprojects {
    apply(plugin = "checkstyle")
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    group = "wtf.choco"
    version = "2.3.0"

    repositories {
        mavenCentral()
        maven { url = uri("http://repo.choco.wtf/releases"); isAllowInsecureProtocol = true }
        maven { url = uri("http://repo.choco.wtf/snapshots"); isAllowInsecureProtocol = true } // Themis
    }

    dependencies {
        compileOnly(rootProject.libs.jetbrains.annotations)
        implementation(rootProject.libs.choco.networking.common)
    }

    java {
        withJavadocJar()
        withSourcesJar()

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks {
        withType<JavaCompile> {
            options.release = 17
            options.encoding = Charsets.UTF_8.name()
        }

        withType<Javadoc> {
            val options = (options as org.gradle.external.javadoc.StandardJavadocDocletOptions)
            options.encoding = Charsets.UTF_8.name()
            
            options.links(
                "https://docs.oracle.com/en/java/javase/17/docs/api/",
                "https://hub.spigotmc.org/javadocs/spigot"
            )

            options.tags(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Specification",
                "implNote:a:Implementation Note"
            )
        }

        withType<Checkstyle> {
            configFile = file("${rootDir}/checkstyle.xml")
        }

        withType<Test> {
            useJUnitPlatform()
        }
    }

    publishing {
        repositories {
            maven {
                isAllowInsecureProtocol = true // I'll fix this once I get an SSL cert for my repository :)

                val repository = if (project.version.toString().endsWith("SNAPSHOT")) "snapshots" else "releases"
                url = uri("http://repo.choco.wtf/$repository")

                credentials {
                    username = project.properties["mavenUsername"].toString()
                    password = project.properties["mavenAccessToken"].toString()
                }
            }
        }

        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }
}
