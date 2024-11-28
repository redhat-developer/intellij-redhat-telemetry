import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

plugins {
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    id("idea")
}

group = "com.redhat.devtools.intellij"
version = providers.gradleProperty("projectVersion").get() // Plugin version
val platformVersion = providers.gradleProperty("ideaVersion").get()
val javaVersion = 17

// https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#runIdeForUiTests
val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
    task {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf(
                "-Dide.mac.message.dialogs.as.sheets=false",
                "-Djb.privacy.policy.text=<!--999.999-->",
                "-Djb.consents.confirmation.enabled=false",
                "-Duser.language=en -Duser.country=US"
            )
        }

        systemProperty("robot-server.port", System.getProperty("robot-server.port"))
        systemProperties["com.redhat.devtools.intellij.telemetry.mode"] = "debug"
    }

    plugins {
        robotServerPlugin()
    }
}

intellijPlatform {

    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("sinceIdeaBuild")
            untilBuild = provider { null }
        }
    }

    publishing {
        token = providers.gradleProperty("jetBrainsToken")
        channels = providers.gradleProperty("jetBrainsChannel").map { listOf(it) }
    }

    pluginVerification {
        ides {
            ide(IntelliJPlatformType.IntellijIdeaCommunity, platformVersion)
        }
        freeArgs = listOf(
            "-mute",
            "TemplateWordInPluginId,TemplateWordInPluginName"
        )
    }
}

dependencies {
    intellijPlatform {
        create(IntelliJPlatformType.IntellijIdeaCommunity, platformVersion)

        instrumentationTools()

        pluginVerifier()

        testFramework(TestFrameworkType.Platform)
    }
    implementation(libs.analytics) {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "com.squareup.retrofit2", module = "retrofit-mock")
        exclude(group = "com.google.auto.value", module = "auto-value-annotations")
    }
    implementation(libs.okio) {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation(libs.gson)

    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.launcher)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)

    testRuntimeOnly(libs.junit.jupiter.engine)
}

val platformTests by intellijPlatformTesting.testIde.registering {
    task {
        // Discover and execute JUnit4-based EventCountsTest
        useJUnit()
        description = "Runs the platform tests."
        group = "verification"
        outputs.upToDateWhen { false }
        mustRunAfter(tasks["test"])
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    runIde {
        systemProperty("com.redhat.devtools.intellij.telemetry.mode", "debug")
    }

    test {
        useJUnitPlatform()
    }

    printProductsReleases {
        channels = listOf(ProductRelease.Channel.EAP)
        types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)
        untilBuild = provider { null }
    }
}

configurations.all {
    exclude(group = "org.slf4j", module = "slf4j-api")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
    withSourcesJar()
}

repositories {
    mavenLocal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}
