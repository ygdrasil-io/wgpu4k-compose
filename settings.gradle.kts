rootProject.name = "KotlinProject"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        // WGPU4K repository
        maven {
            url = uri("https://gitlab.com/api/v4/projects/25805863/packages/maven")
        }

        // Use by rococoa
        maven {
            url = uri("http://repo.maven.cyberduck.io.s3.amazonaws.com/releases")
            isAllowInsecureProtocol = true
        }
    }
}

include(":composeApp")