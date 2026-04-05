import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    id("org.openapi.generator") version "7.4.0"
}

group = "com.calendar"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set(
        System.getProperty(
            "openApiSpecPath",
            "$rootDir/../api/generated/@typespec/openapi3/openapi.yaml"
        )
    )
    outputDir.set("${layout.buildDirectory.get()}/generate-resources/main")
    apiPackage.set("com.calendar.generated.api")
    modelPackage.set("com.calendar.generated.model")
    configOptions.set(
        mapOf(
            "interfaceOnly"        to "true",
            "useSpringBoot3"       to "true",
            "useTags"              to "true",
            "dateLibrary"          to "java8",
            "serializationLibrary" to "jackson"
        )
    )
    generateModelTests.set(false)
    generateApiTests.set(false)
    generateModelDocumentation.set(false)
    generateApiDocumentation.set(false)
}

sourceSets {
    main {
        kotlin {
            srcDir("${layout.buildDirectory.get()}/generate-resources/main/src/main/kotlin")
        }
    }
}

tasks.withType<KotlinCompile> {
    dependsOn("openApiGenerate")
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    val exposedVersion = "0.49.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("org.xerial:sqlite-jdbc:3.45.2.0")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Required by generated code
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
    implementation("io.swagger.core.v3:swagger-models:2.2.20")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
}
