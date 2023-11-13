plugins {
    `java-library`
    `maven-publish`
    id("org.cadixdev.licenser") version "0.6.1"
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.javassist:javassist:3.29.2-GA")
    compileOnlyApi("org.jetbrains:annotations:24.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    create<Exec>("generateExpectations") {
        commandLine = listOf("node", "scripts/generate_expectations.js")
    }
    test {
        useJUnitPlatform()
        dependsOn("generateExpectations")
    }
}

license {
    header.set(rootProject.resources.text.fromFile("header.txt"))
    include("**/*.java")
    newLine = true
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
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