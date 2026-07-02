// Top-level build file where you can add configuration options common to all sub-projects/modules.
val jarFile = file("gradle/wrapper/gradle-wrapper.jar")
val base64File = file("gradle/wrapper/gradle-wrapper.jar.base64")
if (!jarFile.exists() && base64File.exists()) {
    val base64Str = base64File.readText().trim()
    val bytes = java.util.Base64.getDecoder().decode(base64Str)
    jarFile.parentFile.mkdirs()
    jarFile.writeBytes(bytes)
    println("Successfully decoded and restored gradle-wrapper.jar from base64!")
}

val gradlewFile = file("gradlew")
if (gradlewFile.exists()) {
    gradlewFile.setExecutable(true, false)
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

tasks.register("makeGradlewExecutable") {
    doLast {
        val f = file("gradlew")
        f.setExecutable(true, false)
        println("gradlew is now executable: " + f.canExecute())
    }
}

tasks.register<Exec>("runGradlewVersion") {
    dependsOn("makeGradlewExecutable")
    commandLine("sh", file("gradlew").absolutePath, "--version")
}

tasks.register<Exec>("runGradlewAssembleDebug") {
    dependsOn("makeGradlewExecutable")
    commandLine("sh", file("gradlew").absolutePath, "assembleDebug")
}


