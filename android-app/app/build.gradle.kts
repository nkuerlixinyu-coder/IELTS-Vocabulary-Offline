import java.nio.charset.StandardCharsets
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

plugins {
    id("com.android.application")
}

val originalWebRoot = rootProject.layout.projectDirectory.dir("..")
val originalIndex = originalWebRoot.file("index.html")

abstract class PrepareWebIndexTask : DefaultTask() {
    @get:InputFile
    abstract val sourceIndex: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val source = sourceIndex.get().asFile.readText(StandardCharsets.UTF_8)
        check(source.contains("window.BOOK_DATA")) {
            "The original index.html does not contain BOOK_DATA."
        }
        check(source.contains("</body>")) {
            "The original index.html has no closing body tag."
        }

        val adapted = source
            // The original assets directory is mounted as the Android asset root.
            .replace("assets/", "")
            .replace(
                "</body>",
                "  <script src=\"app-enhancements.js\"></script>\n</body>"
            )

        val output = outputDirectory.file("index.html").get().asFile
        output.parentFile.mkdirs()
        output.writeText(adapted, StandardCharsets.UTF_8)
    }
}

val prepareWebIndex = tasks.register<PrepareWebIndexTask>("prepareWebIndex") {
    description = "Adapts the original offline web entry point for Android assets."
    group = "build"
    sourceIndex.set(originalIndex)
    outputDirectory.set(layout.buildDirectory.dir("generated/webAssets"))
}

android {
    namespace = "com.ielts.vocabulary.offline"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ielts.vocabulary.offline"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    androidResources {
        noCompress += setOf("mp3", "jpg", "webp")
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/AL2.0",
            "META-INF/LGPL2.1"
        )
    }
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.apply {
            // Produces audio/, cards/ and pages/ at the packaged asset root without
            // duplicating the 675 MB source library inside the Android project.
            addStaticSourceDirectory("../../assets")
            addGeneratedSourceDirectory(
                prepareWebIndex,
                PrepareWebIndexTask::outputDirectory
            )
        }
    }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.webkit:webkit:1.16.0")
}
