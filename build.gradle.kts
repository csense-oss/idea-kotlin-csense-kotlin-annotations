plugins {
    id("org.jetbrains.intellij") version "0.4.10"
    kotlin("jvm") version "1.3.60"
    java
    id("org.owasp.dependencycheck") version "5.1.0"
}

group = "csense.kotlin"
version = "0.1"


// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    updateSinceUntilBuild = false //Disables updating since-build attribute in plugin.xml
    setPlugins("kotlin", "java")
    version = "2019.2"
}

repositories {
    jcenter()
    //until ds is in jcenter
    maven(url = "https://dl.bintray.com/csense-oss/maven")
}

dependencies {
    implementation("csense.kotlin:csense-kotlin-jvm:0.0.24")
    implementation("csense.kotlin:csense-kotlin-annotations-jvm:0.0.6")
    implementation("csense.kotlin:csense-kotlin-ds-jvm:0.0.24")
}

tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      - first release
      """)
}

tasks.getByName("check").dependsOn("dependencyCheckAnalyze")

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
    this.kotlinOptions.jvmTarget = "1.8"
}