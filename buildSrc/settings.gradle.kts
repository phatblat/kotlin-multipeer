/*
 * settings.gradle.kts
 * Ditto Android buildSrc Gradle project
 */

rootProject.name = "buildSrc"
//enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// https://docs.gradle.org/current/userguide/platforms.html#sec:sharing-catalogs
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
