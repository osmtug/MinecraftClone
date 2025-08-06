plugins {
    application
}

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.2"

dependencies {
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
    implementation("org.joml:joml:1.10.5")

    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-stb:$lwjglVersion:natives-windows")
    
}

application {
    // Remplace "MainKt" par le nom complet de ta classe principale si elle est dans un package
    mainClass.set("MainKt")
}