package com.ishihata_tech.game_sample.bomber

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setForegroundFPS(60)
        setTitle("Bomber")
        setWindowedMode(800, 480)
        setMaximized(true)
        useVsync(true)
    }
    Lwjgl3Application(Game(), config)
}
