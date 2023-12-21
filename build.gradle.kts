import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

plugins {
    kotlin("js") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
}

group = "io.wollinger.animals"
version = "0.0.1"

repositories {
    mavenCentral()
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    }
}


dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.8.0-RC")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-html-js", version = "0.8.0")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.5.1")
    implementation(npm(name = "matter-js", version = "0.19.0"))
}

data class BuildInfo(
    val version: String,
    val githash: String,
    val commitMessage: String,
    val timestamp: Long
) {
    companion object Serializer: SerializationStrategy<BuildInfo> {
        override val descriptor = buildClassSerialDescriptor("BuildInfo") {
            element<String>("version")
            element<String>("githash")
            element<String>("commitMessage")
            element<Long>("timestamp")
        }

        override fun serialize(encoder: Encoder, value: BuildInfo) {
            encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, value.version)
                encodeStringElement(descriptor, 1, value.githash)
                encodeStringElement(descriptor, 2, value.commitMessage)
                encodeLongElement(descriptor, 3, value.timestamp)
            }
        }
    }
}

fun createBuildFile() {
    File("src/main/resources/build.json").apply {
        createNewFile()
        //Create and write buildinfo
        val buildInfo = BuildInfo(
            version = version.toString(),
            githash = gitHash(),
            commitMessage = gitMsg(),
            timestamp = System.currentTimeMillis()
        )
        writeText(Json.encodeToString(
            serializer = BuildInfo.Serializer,
            value = buildInfo
        ))
    }
}

task<Exec>("buildAndRun") {
    dependsOn("browserDistribution")
    workingDir("build/dist/js/productionExecutable")
    commandLine("php", "-S", "127.0.0.1:80")
}

fun exec(cmd: String): String {
    val os = ByteArrayOutputStream()
    project.exec {
        commandLine = cmd.split(" ")
        standardOutput = os
    }
    return String(os.toByteArray()).trim()
}

fun gitHash() = exec("git rev-parse --short HEAD")
fun gitMsg() = exec("git log -1 --pretty=%B")

kotlin {
    js(IR) {
        browser {
            testTask {
                this.enabled = false
            }
            webpackTask {
                createBuildFile()
                this.mainOutputFileName.set("app.js")
            }
        }
        binaries.executable()
    }
}

