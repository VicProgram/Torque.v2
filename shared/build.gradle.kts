plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.test {
    useJUnit()
}
