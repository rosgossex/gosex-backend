plugins {
  id("org.springframework.boot") version "3.2.5"
  id("io.spring.dependency-management") version "1.1.4"
  alias(libs.plugins.ktfmt)
  alias(libs.plugins.kotlin.jvm)
  kotlin("plugin.spring") version "2.1.20"
  kotlin("plugin.jpa") version "2.1.20"
  jacoco
}

group = "gosex.backend"

version = "0.0.1-SNAPSHOT"

java { sourceCompatibility = JavaVersion.VERSION_21 }

kotlin { jvmToolchain(21) }

repositories { mavenCentral() }

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
  implementation(libs.kotlinx.datetime)
  implementation(libs.postgresql)

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation(libs.testcontainers.junit.jupiter)
  testImplementation(libs.testcontainers.postgresql)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
  testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport)

  testLogging {
    events("passed", "failed")
    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    showExceptions = true
    showCauses = true
    showStackTraces = true
  }
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
    html.required.set(true)
    csv.required.set(false)
  }

  sourceSets(sourceSets.main.get())
}

tasks.jacocoTestCoverageVerification {
  violationRules { rule { limit { minimum = "0.5".toBigDecimal() } } }
}

ktfmt {
  kotlinLangStyle()
  blockIndent.set(2)
  continuationIndent.set(2)
}
