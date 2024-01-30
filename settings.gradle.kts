pluginManagement {
    repositories {
        google()
        mavenLocal()
        maven(url = "https://jitpack.io")
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        mavenLocal()
        maven(url = "https://jitpack.io")

    }
}

rootProject.name = "EntertechExternalDeviceCommunication"
include(":app")
include(":entertech_communication_api")
include(":entertech_communication_usb")
include(":entertech_communication_serial_port")
