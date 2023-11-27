plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
//    id ("custom.android.plugin")
    id ("kotlin-kapt")
}

/*PublishInfo {
    groupId = "cn.entertech.android" // 库的组织，使用域名表示
    artifactId = "device_communicate_usb" // 库名称
    version = "0.0.1-debug" // 库版本
}*/


android {
    namespace = "cn.entertech.communication.usb"
    compileSdk = 33

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    kapt ("com.google.auto.service:auto-service:1.0-rc6")
    implementation ("com.google.auto.service:auto-service:1.0-rc6")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.github.mik3y:usb-serial-for-android:3.7.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation(project(mapOf("path" to ":entertech_communication_api")))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}