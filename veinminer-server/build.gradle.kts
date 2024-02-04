dependencies {
    implementation(project(":veinminer-common"))

    compileOnly(libs.gson)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks {
    withType<Javadoc>() {
        exclude("wtf/choco/veinminer/command/**")
    }
}
