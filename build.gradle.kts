plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
    java
    id("io.gitlab.arturbosch.detekt") version ("1.23.8")
    id("com.ncorti.ktfmt.gradle") version "0.22.0"
    application
    idea
}

group = "com.gulaii"

version = "0.1.0"

repositories { mavenCentral() }

dependencies {
    implementation("io.lettuce:lettuce-core:6.2.3.RELEASE")
    implementation("org.slf4j:slf4j-api:1.7.36")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> { useJUnitPlatform() }

detekt {
    config.setFrom(files("${projectDir}/config/detekt.yml"))
    buildUponDefaultConfig = true
}

ktfmt {
    kotlinLangStyle()
    maxWidth.set(80)
    removeUnusedImports.set(false)
    manageTrailingCommas.set(true)
}

sourceSets {
    main {
        kotlin { srcDir("src") }
        resources { srcDirs("config") }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") { from(components["java"]) }
    }
}
