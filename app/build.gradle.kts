import java.util.Properties
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.io.FileInputStream

plugins {
    id("com.android.application")
}
    val properties = Properties()
    properties.load(project.rootProject.file("local.properties").inputStream())

android {
    namespace = "com.example.rhythmproto"
    compileSdk = 34

    val localProperties = Properties()
    localProperties.load(FileInputStream(rootProject.file("local.properties")))

    buildFeatures{
        buildConfig = true
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.example.rhythmproto"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "KAKAO_API_KEY", properties.getProperty("KAKAO_API_KEY"))
        manifestPlaceholders["KAKAO_URI"] = properties["KAKAO_URI"] as String
    }



    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


dependencies {
    implementation ("com.airbnb.android:lottie:6.4.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.kakao.sdk:v2-user:2.6.0")//카톡 로그인
    implementation ("com.github.bumptech.glide:glide:4.11.0")//이미지 핸들링용 글라이드
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
android.buildFeatures.buildConfig = true