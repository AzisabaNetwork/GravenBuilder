plugins {
    java
    `java-library`
    `maven-publish`
}

group = "net.azisaba"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

val dockerJavaVersion = "3.2.13"

dependencies {
    implementation("com.github.docker-java:docker-java-core:$dockerJavaVersion")
    implementation("com.github.docker-java:docker-java-transport-httpclient5:$dockerJavaVersion")
    compileOnlyApi("com.github.docker-java:docker-java-core:$dockerJavaVersion")
    compileOnlyApi("com.github.docker-java:docker-java-transport-httpclient5:$dockerJavaVersion")
    compileOnlyApi("org.jetbrains:annotations:23.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
