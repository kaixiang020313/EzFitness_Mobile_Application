

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.chaquo.python")
}

android {
    namespace = "com.example.ezfitness"
    compileSdk = 34

    chaquopy {
        defaultConfig { }
        productFlavors { }
        sourceSets {}
    }

    defaultConfig {
        applicationId = "com.example.ezfitness"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    dependencies {
        implementation("androidx.core:core-ktx:1.12.0")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
        implementation("com.google.firebase:firebase-firestore-ktx:24.10.2")
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
        implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
        implementation("com.google.firebase:firebase-analytics")
        implementation ("com.google.firebase:firebase-storage-ktx")
        implementation("com.google.firebase:firebase-database-ktx:20.3.0")
        implementation ("com.github.bumptech.glide:glide:4.12.0")
        annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
        implementation ("com.google.guava:guava:31.0.1-jre")
        implementation ("org.python:jython-standalone:2.7.2")
        implementation ("commons-io:commons-io:2.11.0")


        //kx
        implementation ("androidx.fragment:fragment-ktx:1.3.0")
        implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
        implementation("androidx.legacy:legacy-support-v4:1.0.0")
        implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
        implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
        implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
        implementation ("com.squareup.okhttp3:okhttp:4.9.1")
        implementation ("org.tensorflow:tensorflow-lite:2.8.0")

        implementation ("com.squareup.retrofit2:retrofit:2.9.0")
        implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation ("com.squareup.okhttp3:okhttp:4.9.0")
        implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0")
        implementation ("com.squareup.okhttp3:okhttp:4.9.2")
    }
}
dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.test:monitor:1.6.1")
}
