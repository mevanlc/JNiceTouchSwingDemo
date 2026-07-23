pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "JNiceTouchSwingDemo"

includeBuild("../JNiceTouch") {
    dependencySubstitution {
        substitute(module("io.github.mevanlc:jnicetouch"))
            .using(project(":lib"))
    }
}
