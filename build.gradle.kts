import net.minecraftforge.gradle.userdev.UserDevExtension
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency
import java.io.ByteArrayOutputStream

buildscript {
    repositories {
        maven { url = uri("https://maven.minecraftforge.net") }
        mavenCentral()
    }
    dependencies {
        classpath(group = "net.minecraftforge.gradle", name = "ForgeGradle", version = "5.1.+") {
            isChanging = true
        }
    }
}

val worldRebuilderId = findProperty("worldRebuilder.id") as String
val worldRebuilderName = findProperty("worldRebuilder.name") as String
val worldRebuilderVersion = findProperty("worldRebuilder.version") as String
val forgeVersion = findProperty("forge.version") as String
val spongeApiVersion = findProperty("sponge-api.version") as String
val minecraftVersion = findProperty("minecraft.version") as String

plugins {
    java
    `java-library`
    idea
    `maven-publish`
    id("org.spongepowered.gradle.plugin") version "2.1.1"
    id("org.spongepowered.gradle.ore") version "2.1.1" // for Ore publishing
}

apply(plugin = "net.minecraftforge.gradle")

group = "io.github.aquerr"
version = "$worldRebuilderVersion-API-$spongeApiVersion"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    "minecraft"("net.minecraftforge:forge:${forgeVersion}")
    api("org.spongepowered:spongeapi:${spongeApiVersion}")
}

tasks {
    jar {
        finalizedBy("reobfJar")
        if(System.getenv("JENKINS_HOME") != null) {
            project.version = project.version.toString() + "_" + System.getenv("BUILD_NUMBER")
            println("File name => " + archiveBaseName.get())
        } else {
            project.version = project.version.toString() + "-SNAPSHOT"
        }
    }
}

configure<UserDevExtension> {
    mappings("official", minecraftVersion)
}

sponge {
    apiVersion(spongeApiVersion)
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin(worldRebuilderId) {
        displayName(worldRebuilderName)
        version(worldRebuilderVersion)
        entrypoint("io.github.aquerr.worldrebuilder.WorldRebuilder")
        description("Rebuilds destroyed blocks after specified time.")
        links {
            homepage("https://github.com/Aquerr/WorldRebuilder")
            source("https://github.com/Aquerr/WorldRebuilder")
            issues("https://github.com/Aquerr/WorldRebuilder/issues")
        }
        contributor("Aquerr") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val getGitCommitDesc by tasks.registering(Exec::class) {
    commandLine("git", "log", "-1", "--pretty=%B")
    standardOutput = ByteArrayOutputStream()
    doLast {
        project.extra["gitCommitDesc"] = standardOutput.toString()
    }
}

tasks.create("publishBuildOnDiscord") {
    dependsOn(getGitCommitDesc)
    group = "Publishing"
    description = "Task for publishing the jar file to discord's jenkins channel"
    doLast {
        val jarFiles: List<String> = groovy.ant.FileNameFinder().getFileNames(project.buildDir.path, "**/*.jar")

        if(jarFiles.size > 0) {
            println("Found jar files: " + jarFiles)

            var lastCommitDescription = project.extra["gitCommitDesc"]
            if(lastCommitDescription == null || lastCommitDescription == "") {
                lastCommitDescription = "No changelog provided"
            }

            exec {
                commandLine("java", "-jar",  ".." + File.separator + "jenkinsdiscordbot-1.0.jar", "WorldRebuilder", jarFiles[0], lastCommitDescription)
            }
        }
    }
}

tasks.register("printEnvironment") {
    doLast {
        System.getenv().forEach { key, value ->
            println("$key -> $value")
        }
    }
}

oreDeployment {
    // The default publication here is automatically configured by SpongeGradle
    // using the first-created plugin's ID as the project ID
    // A version body is optional, to provide additional information about the release
    /*
    defaultPublication {
        // Read the version body from the file whose path is provided to the changelog gradle property
        versionBody.set(providers.gradleProperty("changelog").map { file(it).readText(Charsets.UTF_8) }.orElse(""))
    }*/
}

publishing {

    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/Aquerr/WorldRebuilder")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_PUBLISHING_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_PUBLISHING_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>(worldRebuilderId)
        {
            artifactId = worldRebuilderId
            description = project.description

            from(components["java"])
        }
    }
}