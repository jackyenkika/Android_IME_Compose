import com.android.build.gradle.api.ApkVariantOutput
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
//    alias(libs.plugins.ksp)
}

val outputDate =
    SimpleDateFormat("yyyyMMdd").apply { timeZone = TimeZone.getTimeZone("Asia/Taipei") }
        .format(Date()).toInt() // This will give you the date string e.g., "20231027"

val projectName: String = "common"  // for 可指定項目專案名稱來做特別判斷

val targetVersion: String = "" //如果非正式發版，可使用此更改最後的version。舉例:alpha01,beta01

android {
    signingConfigs {
        create("kika") {
            val ksFile = rootProject.file("../keystore/kika.properties")
            val props = Properties()
            if (ksFile.canRead()) {
                props.load(FileInputStream(ksFile))
                storeFile = file(props.getProperty("storeFile"))
                keyPassword = props.getProperty("keyPassword")
                keyAlias = props.getProperty("keyAlias")
                storePassword = props.getProperty("storePassword")
                println("The key for device has been setup")
            } else {
                println("\'keystore.properties\' not found!")
            }
        }
    }

    buildFeatures.buildConfig = true
    namespace = "com.iqqi.ime"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.kika.imeservice"
        minSdk = 28
        targetSdk = 36
        versionCode = outputDate
        versionName = // 版本名稱會自動加上前綴
            if (targetVersion == "") {
                "ime-${projectName}-${outputDate}"
            } else {
                "ime-${projectName}-${outputDate}-${targetVersion}"
            }
        buildConfigField("String", "ProjectName", "\"$projectName\"")

        ndk {
            abiFilters.add("arm64-v8a")
//            abiFilters.add("armeabi-v7a")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("kika")

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    applicationVariants.configureEach {
        val variant = this
        variant.outputs.configureEach {
            // 'this' refers to an ApkVariantOutput object
            val output = this as ApkVariantOutput
            val newApkName = "${variant.versionName}.apk"
            output.outputFileName = newApkName
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.savedstate.ktx)
    implementation(libs.androidx.lifecycle.service)

    implementation(project(":kika-t9-engine-library"))
    implementation(project(":kika-pinyin-engine-library"))
}