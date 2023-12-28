plugins {
    `java-library`
    `maven-publish`
    id("org.cadixdev.licenser") version "0.6.1"
    id("me.champeau.jmh") version "0.7.2"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/bedrockk/MoLang")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
    maven("https://maven.blamejared.com/") // moonflower's molang-compiler
}

dependencies {
    api("org.javassist:javassist:3.30.1-GA")
    compileOnlyApi("org.jetbrains:annotations:24.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // performance comparison with other libraries
    jmhImplementation("com.bedrockk:molang:1.0-SNAPSHOT")
    jmhImplementation("gg.moonflower:molang-compiler:3.0.4.16")
}

tasks {
    create<Exec>("generateExpectations") {
        commandLine = listOf("node", "scripts/generate_expectations.js")
    }
    test {
        useJUnitPlatform()
        dependsOn("generateExpectations")
    }
    compileJmhJava {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    compileJava {
        sourceCompatibility = "8"
        targetCompatibility = "8"
    }
}

license {
    header.set(rootProject.resources.text.fromFile("header.txt"))
    include("**/*.java")
    newLine = false
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val repositoryName: String by project
val snapshotRepository: String by project
val releaseRepository: String by project

publishing {
    repositories {
        maven {
            val snapshot = project.version.toString().endsWith("-SNAPSHOT")

            name = repositoryName
            url = if (snapshot) {
                uri(snapshotRepository)
            } else {
                uri(releaseRepository)
            }
            credentials(PasswordCredentials::class)
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}