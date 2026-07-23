import org.panteleyev.jpackage.ImageType

plugins {
    application
    id("org.panteleyev.jpackageplugin") version "2.1.0"
}

group = "io.github.mevanlc"
version = "0.1.0"

base {
    archivesName = "jnicetouch-swing-demo"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.mevanlc:jnicetouch:0.1.0-SNAPSHOT")
    testImplementation("org.testng:testng:7.10.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

application {
    mainClass = "io.github.mevanlc.jnicetouch.swingdemo.JNiceTouchSwingDemo"
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
        "-Dapple.awt.application.name=JNiceTouch Swing Demo",
        "-Dapple.awt.application.appearance=system",
    )
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

tasks.named<Test>("test") {
    useTestNG()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

val jpackageInput = layout.buildDirectory.dir("jpackage-input")
val jpackageOutput = layout.buildDirectory.dir("jpackage-output")
val appBundle = jpackageOutput.map { it.dir("JNiceTouch Swing Demo.app") }
val installedAppBundle = file("/Applications/JNiceTouch Swing Demo.app")
val appIconSvg = layout.projectDirectory.file("appicon.svg")
val appIconPng = layout.projectDirectory.file("appicon.png")
val appIconIcns = layout.projectDirectory.file("appicon.icns")

val renderAppIconPng by tasks.registering(Exec::class) {
    group = "build"
    description = "Renders the shared SVG as the high-resolution Swing icon."
    inputs.file(appIconSvg)
    outputs.file(appIconPng)
    commandLine(
        "rsvg-convert",
        "--width", "1024",
        "--height", "1024",
        "--output", appIconPng.asFile.absolutePath,
        appIconSvg.asFile.absolutePath,
    )
}

val makeAppIconIcns by tasks.registering(Exec::class) {
    group = "build"
    description = "Builds the macOS ICNS from the shared high-resolution PNG."
    dependsOn(renderAppIconPng)
    inputs.file(appIconPng)
    outputs.file(appIconIcns)
    commandLine(
        "makeicns",
        "-in", appIconPng.asFile.absolutePath,
        "-out", appIconIcns.asFile.absolutePath,
    )
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(renderAppIconPng)
    from(appIconPng)
}

val prepareJpackageInput by tasks.registering(Sync::class) {
    dependsOn(tasks.named("jar"))
    from(tasks.named("jar"))
    from(configurations.runtimeClasspath)
    into(jpackageInput)
}

tasks.jpackage {
    dependsOn(prepareJpackageInput, makeAppIconIcns)
    input = jpackageInput
    destination = jpackageOutput
    type = ImageType.APP_IMAGE
    appName = "JNiceTouch Swing Demo"
    appVersion = "1.0.0"
    vendor = "mevanlc"
    appDescription = "Interactive source example for JNiceTouch"
    copyright = "Copyright 2026 mevanlc"
    mainJar = "jnicetouch-swing-demo-${project.version}.jar"
    mainClass = application.mainClass.get()
    icon = appIconIcns
    javaOptions = application.applicationDefaultJvmArgs
    mac {
        macPackageIdentifier = "io.github.mevanlc.jnicetouch.swingdemo"
        macPackageName = "JNiceTouch Swing Demo"
    }
}

tasks.register("packageMacApp") {
    group = "distribution"
    description = "Builds the self-contained macOS demo application."
    dependsOn(tasks.jpackage)
}

val removeInstalledMacApp by tasks.registering(Delete::class) {
    dependsOn(tasks.jpackage)
    delete(installedAppBundle)
}

tasks.register<Exec>("macDeployToApplications") {
    group = "distribution"
    description = "Deploys the packaged demo to /Applications."
    dependsOn(removeInstalledMacApp)
    commandLine(
        "/usr/bin/ditto",
        appBundle.get().asFile.absolutePath,
        installedAppBundle.absolutePath,
    )
}
