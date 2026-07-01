plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight") version "2.0.2"
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
    implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
    testImplementation("junit:junit:4.13.2")
}

sqldelight {
    databases {
        create("TorqueDatabase") {
            packageName.set("com.example.torque.desktop.db")
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.example.torque.desktop.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
            )
            packageName = "Torque"
            packageVersion = "1.0.0"
            description = "Gestion de mantenimiento de motocicletas"
            vendor = "Torque"
            windows {
                menuGroup = "Torque"
                upgradeUuid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
            }
            macOS {
                bundleID = "com.example.torque.desktop"
            }
            linux {
                packageName = "torque-desktop"
            }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
