import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")

}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")  // Обновлено
    implementation("org.xerial:sqlite-jdbc:3.45.1.0") // Обновлено
    implementation("org.jetbrains.exposed:exposed-core:0.43.0")  // Обновлено
    implementation("org.jetbrains.exposed:exposed-dao:0.43.0")  // Обновлено
    implementation("org.jetbrains.exposed:exposed-jdbc:0.43.0")  // Обновлено
    implementation("org.jetbrains.compose.desktop:desktop-jvm:1.5.0")
    implementation("com.google.code.gson:gson:2.8.8")

    implementation("org.jetbrains.compose.ui:ui:1.5.10")
    implementation("org.jetbrains.compose.foundation:foundation:1.5.10")
    implementation("org.jetbrains.compose.material:material:1.5.10")
    implementation("org.jetbrains.compose.runtime:runtime:1.5.10")
    implementation("org.jetbrains.compose.desktop:desktop:1.5.10")
//    implementation("com.google.accompanist:accompanist-svg:0.32.0")
//    implementation("io.coil-kt:coil-compose:2.4.0")


}

compose.desktop {
    application {

        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "kp"
            packageVersion = "1.0.0"
            modules("java.sql")
        }
    }
}
