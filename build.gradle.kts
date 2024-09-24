plugins {
    kotlin("jvm") version "2.0.20"
    id("jacoco")
}

group = "org.capital.gains"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    testImplementation(kotlin("test"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    reports {
        xml.required = false
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }

    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching{
            exclude("**/org/capital/gains/Main*")
        }
    )
}

tasks.test {
    useJUnitPlatform()
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}


tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "CLASS"

            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }

            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching{
            exclude("**/org/capital/gains/Main*")
        }
    )
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.capital.gains.MainKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}