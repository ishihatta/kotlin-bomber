package com.ishihata_tech.game_sample.bomber

import com.badlogic.gdx.Game
import com.ishihata_tech.game_sample.bomber.game_screen.GameScreen
import com.ishihata_tech.game_sample.bomber.game_screen.PlayerType
import com.ishihata_tech.game_sample.bomber.main_menu.MainMenuScreen

class MyGame : Game() {
    override fun create() {
        setScreen(MainMenuScreen(this))
    }

    override fun dispose() {
        super.dispose()
        getScreen()?.dispose()
    }

    fun goToGameScreen(playerType1: PlayerType, playerType2: PlayerType) {
        val previousScreen = getScreen()
        setScreen(GameScreen(this, playerType1, playerType2))
        previousScreen.dispose()
    }

    fun returnToMainMenu() {
        val previousScreen = getScreen()
        setScreen(MainMenuScreen(this))
        previousScreen.dispose()
    }
}