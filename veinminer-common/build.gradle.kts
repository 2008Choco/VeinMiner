dependencies {
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
